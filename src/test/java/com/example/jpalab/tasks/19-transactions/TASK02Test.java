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
 * Тема 19, задание 02. noRollbackFor: RuntimeException из списка noRollbackFor
 * НЕ откатывает транзакцию — изменения закоммичены, несмотря на исключение.
 *
 * Контракт задания (см. tasks/19-transactions/README.md):
 * сервис com.example.jpalab.service.ProductService (бин "productService"),
 * метод
 *   void bulkUpdatePrices(int minStock, BigDecimal newPrice).
 * Поведение: всем товарам с stockQuantity >= minStock установить цену
 * newPrice, flush; затем выбросить PriceSyncException
 * (com.example.jpalab.exception, наследник RuntimeException — создаёт
 * студент). Изменения цен обязаны дожить до commit.
 *
 * Тест НЕ транзакционный: реальные транзакции — только через
 * TransactionTemplate. До реализации тест красный: у сервиса нет метода.
 */
@SpringBootTest
class TASK02Test {

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

    private BigDecimal priceOf(Long id) {
        return tx.execute(status -> (BigDecimal) em.createNativeQuery(
                        "select price from products where id = :id")
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
    void noRollbackForCommitsUpdateDespiteRuntimeException() {
        Product p1 = seedProduct("T19-02 P1", new BigDecimal("1.00"), 5);
        Product p2 = seedProduct("T19-02 P2", new BigDecimal("2.00"), 10);
        Product p3 = seedProduct("T19-02 P3", new BigDecimal("3.00"), 1);

        Throwable thrown = catchThrowable(() -> TaskTestSupport.invoke(service(), "bulkUpdatePrices",
                new Class<?>[]{int.class, BigDecimal.class}, 3, new BigDecimal("999.99")));

        assertThat(thrown).as("метод выбрасывает RuntimeException (PriceSyncException)")
                .isInstanceOf(RuntimeException.class);
        assertThat(thrown.getClass().getSimpleName())
                .as("класс исключения называется PriceSyncException")
                .isEqualTo("PriceSyncException");

        assertThat(priceOf(p1.getId())).as("цена закоммичена, несмотря на исключение")
                .isEqualByComparingTo("999.99");
        assertThat(priceOf(p2.getId())).as("вторая цена тоже закоммичена")
                .isEqualByComparingTo("999.99");
        assertThat(priceOf(p3.getId())).as("stock < minStock — цена не тронута")
                .isEqualByComparingTo("3.00");
    }
}
