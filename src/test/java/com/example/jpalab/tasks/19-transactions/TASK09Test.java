package com.example.jpalab.tasks.t19;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.OrderRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 19, задание 09. Изолированная внутренняя граница транзакции.
 * Внутренняя часть откатывается целиком, а внешняя транзакция продолжает
 * работу и коммитится.
 *
 * Контракт задания (см. tasks/19-transactions/README.md):
 * сервис com.example.jpalab.service.ProductService (бин "productService"),
 * метод
 *   boolean transferStockSafely(Long fromId, Long toId, int amount).
 * Поведение: transferStockSafely выполняется во внешней @Transactional
 * транзакции. Сам перенос склада выполняется в отдельной внутренней
 * транзакции с propagацией REQUIRES_NEW — декларативно (отдельный метод
 * @Transactional(REQUIRES_NEW) + вызов через прокси самого сервиса) или
 * программно (TransactionTemplate с явной
 * DefaultTransactionDefinition(Propagation.REQUIRES_NEW.value())).
 * Внутри: сначала прибавить amount получателю и flush; затем в памяти
 * снять amount у источника; если остаток уйдёт в минус — IllegalArgumentException
 * ДО flush источника (поле stockQuantity ограничено @PositiveOrZero). Сбой
 * внутренней транзакции откатывает только её изменения, включая уже
 * записанный UPDATE получателя. transferStockSafely перехватывает сбой,
 * сохраняет Order (лог попытке по источнику) и возвращает false; при успехе
 * возвращает true. Order сохраняется в обоих случаях и закоммичен внешней
 * транзакцией.
 *
 * Примечание: propagation NESTED в этом стеке недоступен —
 * JpaTransactionManager (JpaDialect) не поддерживает savepoints (проверено
 * экспериментально), а Hibernate 6 не имеет Session.savepoint. Семантика
 * "откат только внутренней части" достигается внутренней транзакцией.
 *
 * Тест НЕ транзакционный: прямые вызовы. До реализации тест красный: у
 * сервиса нет метода.
 */
@SpringBootTest
class TASK09Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ApplicationContext applicationContext;

    private TransactionTemplate tx;

    @BeforeEach
    void setUp() {
        tx = new TransactionTemplate(transactionManager);
    }

    private Object service() {
        return applicationContext.getBean("productService");
    }

    private Product seedProduct(String name, BigDecimal price, int stock) {
        return tx.execute(status -> {
            Product p = new Product();
            p.setName(name);
            p.setPrice(price);
            p.setStockQuantity(stock);
            return productRepository.save(p);
        });
    }

    private int stockOf(Long id) {
        return tx.execute(status -> (Integer) em.createNativeQuery(
                        "select stock_quantity from products where id = :id")
                .setParameter("id", id).getSingleResult());
    }

    private long orderCountOf(Long productId) {
        return tx.execute(status -> (Long) em.createNativeQuery(
                        "select count(*) from orders where product_id = :id")
                .setParameter("id", productId).getSingleResult());
    }

    @AfterEach
    void cleanup() {
        tx.executeWithoutResult(status -> {
            orderRepository.deleteAll();
            productRepository.deleteAll();
            categoryRepository.deleteAll();
        });
    }

    @Test
    void innerBoundaryRollsBackOnlyInnerPart() {
        Product fromA = seedProduct("T19-09 FromA", new BigDecimal("10.00"), 10);
        Product toA = seedProduct("T19-09 ToA", new BigDecimal("20.00"), 5);
        Product fromB = seedProduct("T19-09 FromB", new BigDecimal("30.00"), 2);

        // Случай A: достаточно — перенос закоммичен, заказ залогирован
        Boolean a = TaskTestSupport.invoke(service(), "transferStockSafely",
                new Class<?>[]{Long.class, Long.class, int.class}, fromA.getId(), toA.getId(), 4);
        assertThat(a).as("перенос завершился").isTrue();
        assertThat(stockOf(fromA.getId())).isEqualTo(6);
        assertThat(stockOf(toA.getId())).isEqualTo(9);
        assertThat(orderCountOf(fromA.getId())).as("заказ (попытка) залогирован").isEqualTo(1);

        // Случай B: не хватает — внутренняя транзакция откатилась (вместе с
        // UPDATE получателя), внешняя (Order + возврат false) закоммичена
        Boolean b = TaskTestSupport.invoke(service(), "transferStockSafely",
                new Class<?>[]{Long.class, Long.class, int.class}, fromB.getId(), toA.getId(), 5);
        assertThat(b).as("метод поймал сбой внутренней границы").isFalse();
        assertThat(stockOf(fromB.getId())).as("внутренняя граница: списание откатилось — склад исходный").isEqualTo(2);
        assertThat(stockOf(toA.getId())).as("получатель неудачной попытки не изменился").isEqualTo(9);
        assertThat(orderCountOf(fromB.getId())).as("неудачная попытка тоже залогирована (внешняя часть закоммичена)")
                .isEqualTo(1);
    }
}
