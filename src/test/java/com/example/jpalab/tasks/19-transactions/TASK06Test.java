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
 * Тема 19, задание 06. rollbackFor: по умолчанию ПРОВЕРЯЕМОЕ (checked)
 * исключение транзакцию НЕ откатывает (коммит!), поэтому rollback обязателен
 * объявить явно.
 *
 * Контракт задания (см. tasks/19-transactions/README.md):
 * студент создаёт ПРОВЕРЯЕМОЕ исключение
 * com.example.jpalab.exception.OverstockLimitException (наследник Exception,
 * НЕ RuntimeException);
 * сервис com.example.jpalab.service.ProductService (бин "productService"),
 * метод
 *   void adjustStock(Long productId, int delta) throws OverstockLimitException.
 * Поведение: приписать delta к складу и flush; если склад стал больше 1000 —
 * выбросить OverstockLimitException. Транзакция обязана откатиться.
 *
 * Тест НЕ транзакционный. До реализации тест красный: у сервиса нет метода,
 * класса исключения тоже нет.
 */
@SpringBootTest
class TASK06Test {

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
    void rollbackForRollsBackOnCheckedException() {
        Product p = seedProduct("T19-06 Product", new BigDecimal("10.00"), 500);

        Throwable thrown = catchThrowable(() -> TaskTestSupport.invoke(service(), "adjustStock",
                new Class<?>[]{Long.class, int.class}, p.getId(), 600));

        // TaskTestSupport.invoke заворачивает checked-исключение в AssertionError —
        // достаём оригинал из cause.
        Throwable cause =
                (thrown instanceof AssertionError && thrown.getCause() != null) ? thrown.getCause() : thrown;

        assertThat(cause).as("метод выбрасывает ПРОВЕРЯЕМОЕ исключение (не RuntimeException, не Error)")
                .isNotInstanceOf(RuntimeException.class)
                .isNotInstanceOf(Error.class);
        assertThat(cause.getClass().getSimpleName())
                .as("класс исключения называется OverstockLimitException")
                .isEqualTo("OverstockLimitException");
        assertThat(stockOf(p.getId()))
                .as("rollbackFor: UPDATE откатился, несмотря на checked-исключение")
                .isEqualTo(500);
    }
}
