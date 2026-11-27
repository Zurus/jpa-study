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
 * Тема 19, задание 08. readOnly: подсказка "транзакция только на чтение".
 * В этом стеке (Hibernate 6 + H2) изменение managed-сущности в readOnly-
 * транзакции никуда не записывается и исключения не порождает — это
 * оптимизация, а не guard (persist, в отличие от dirty-UPDATE, не
 * блокируется).
 *
 * Контракт задания (см. tasks/19-transactions/README.md):
 * сервис com.example.jpalab.service.ProductService (бин "productService"),
 * метод
 *   int previewStockAfterPromo(Long id, int promoDelta)
 * с @Transactional(readOnly = true).
 * Поведение: загрузить товар и вернуть stockQuantity + promoDelta.
 * БД не меняется.
 *
 * Тест НЕ транзакционный: прямой вызов метода. До реализации тест красный:
 * у сервиса нет метода.
 */
@SpringBootTest
class TASK08Test {

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

    private long countProducts(String name) {
        return tx.execute(status -> (Long) em.createNativeQuery(
                        "select count(*) from products where name = :n")
                .setParameter("n", name).getSingleResult());
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
    void readOnlyHintDoesNotPersistManagedChange() {
        Product p = seedProduct("T19-08 Product", new BigDecimal("10.00"), 10);

        int preview = TaskTestSupport.invoke(service(), "previewStockAfterPromo",
                new Class<?>[]{Long.class, int.class}, p.getId(), 5);

        assertThat(preview).as("метод возвращает stock + promoDelta").isEqualTo(15);
        assertThat(stockOf(p.getId())).as("readOnly-транзакция не записала изменение в БД").isEqualTo(10);
        assertThat(countProducts("T19-08 Product")).as("новых строк не появилось").isEqualTo(1);
    }
}
