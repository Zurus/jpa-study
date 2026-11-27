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
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Тема 19, задание 01. Атомарность: @Transactional откатывает уже записанные
 * UPDATE, если метод завершился unchecked-исключением.
 *
 * Контракт задания (см. tasks/19-transactions/README.md):
 * сервис com.example.jpalab.service.ProductService (бин "productService"),
 * метод
 *   void transferStock(Long fromId, Long toId, int amount).
 * Поведение: сначала прибавить amount на склад получателя и flush (записать);
 * затем в памяти снять amount со склада источника; если склад источника уйдёт в
 * минус — IllegalArgumentException ДО flush источника (внимание: у поля
 * stockQuantity стоит @PositiveOrZero, записать отрицательный остаток в БД нельзя —
 * его перехватит валидатор). Транзакция откатывается целиком, включая уже
 * записанный UPDATE получателя. Если остаток в норме — перенос закоммичен.
 *
 * Тест НЕ транзакционный: реальные транзакции — только через
 * TransactionTemplate (H2 READ COMMITTED). До реализации тест красный:
 * у сервиса нет метода transferStock.
 */
@SpringBootTest
class TASK01Test {

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

    @AfterEach
    void cleanup() {
        tx.executeWithoutResult(status -> {
            orderRepository.deleteAll();
            productRepository.deleteAll();
            categoryRepository.deleteAll();
        });
    }

    @Test
    void transferStockRollsBackPartialUpdateOnInsufficientStock() {
        Product from = seedProduct("T19-01 From", new BigDecimal("10.00"), 10);
        Product to = seedProduct("T19-01 To", new BigDecimal("20.00"), 5);

        // Случай A: достаточно — перенос закоммичен
        TaskTestSupport.invoke(service(), "transferStock",
                new Class<?>[]{Long.class, Long.class, int.class},
                from.getId(), to.getId(), 4);
        assertThat(stockOf(from.getId())).as("источник после commit").isEqualTo(6);
        assertThat(stockOf(to.getId())).as("получатель после commit").isEqualTo(9);

        // Случай B: не хватает — исключение, уже записанный UPDATE откатывается
        Product from2 = seedProduct("T19-01 From2", new BigDecimal("30.00"), 3);
        Product to2 = seedProduct("T19-01 To2", new BigDecimal("40.00"), 0);

        Throwable thrown = catchThrowable(() -> TaskTestSupport.invoke(service(), "transferStock",
                new Class<?>[]{Long.class, Long.class, int.class},
                from2.getId(), to2.getId(), 5));

        assertThat(thrown).as("нехватка склада — IllegalArgumentException")
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(stockOf(from2.getId())).as("rollback: источник не изменился").isEqualTo(3);
        assertThat(stockOf(to2.getId())).as("получатель тоже не изменился").isEqualTo(0);
    }
}
