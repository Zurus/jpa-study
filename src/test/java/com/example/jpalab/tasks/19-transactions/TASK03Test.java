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
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * Тема 19, задание 03. REQUIRES_NEW: метод с собственным уровнем
 * propagation создаёт НОВУЮ транзакцию (вызывающая — на паузе), поэтому
 * её commit переживает rollback внешней транзакции.
 *
 * Контракт задания (см. tasks/19-transactions/README.md):
 * сервис com.example.jpalab.service.ProductService (бин "productService"),
 * метод
 *   void createProductWithCategory(String productName, String categoryName).
 * Поведение: создать новую Category и новый Product (цена 0, склад 0,
 * product.category = новая категория) и закоммитить их в собственной
 * транзакции.
 *
 * Тест НЕ транзакционный: внешняя транзакция — TransactionTemplate теста,
 * внутри неё вызов метода студента, затем сбой внешней транзакции.
 * До реализации тест красный: у сервиса нет метода.
 */
@SpringBootTest
class TASK03Test {

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

    private long countProducts(String name) {
        return tx.execute(status -> (Long) em.createNativeQuery(
                        "select count(*) from products where name = :n")
                .setParameter("n", name).getSingleResult());
    }

    private long countCategories(String name) {
        return tx.execute(status -> (Long) em.createNativeQuery(
                        "select count(*) from categories where name = :n")
                .setParameter("n", name).getSingleResult());
    }

    private Long idOfProduct(String name) {
        return tx.execute(status -> (Long) em.createNativeQuery(
                        "select id from products where name = :n")
                .setParameter("n", name).getSingleResult());
    }

    private Long idOfCategory(String name) {
        return tx.execute(status -> (Long) em.createNativeQuery(
                        "select id from categories where name = :n")
                .setParameter("n", name).getSingleResult());
    }

    private Long categoryIdOfProduct(Long productId) {
        return tx.execute(status -> (Long) em.createNativeQuery(
                        "select category_id from products where id = :id")
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
    void requiresNewCommitsWhileOuterRollsBack() {
        String probeName = "T19-03 Probe";
        String newName = "T19-03 Created";
        String newCat = "T19-03 CreatedCat";

        // Внешняя транзакция: persist пробного товара -> вызов метода
        // студента -> изменение пробного товара -> сбой (rollback внешней).
        RuntimeException outerFailure = catchThrowableOfType(() -> tx.execute(status -> {
            Product probe = new Product();
            probe.setName(probeName);
            probe.setPrice(new BigDecimal("10.00"));
            probe.setStockQuantity(1);
            em.persist(probe);

            TaskTestSupport.invoke(service(), "createProductWithCategory",
                    new Class<?>[]{String.class, String.class}, newName, newCat);

            probe.setPrice(new BigDecimal("123.45"));
            throw new IllegalStateException("сбой внешней транзакции");
        }), IllegalStateException.class);

        assertThat(outerFailure).as("внешняя транзакция оборвана исключением").isNotNull();

        assertThat(countProducts(probeName)).as("пробный товар откатился вместе с внешней транзакцией").isZero();
        assertThat(countProducts(newName)).as("REQUIRES_NEW: товар закоммичен, несмотря на rollback внешней")
                .isEqualTo(1);
        assertThat(countCategories(newCat)).as("REQUIRES_NEW: категория закоммичена тоже")
                .isEqualTo(1);

        Long productId = idOfProduct(newName);
        Long categoryId = idOfCategory(newCat);
        assertThat(categoryIdOfProduct(productId)).as("товар ссылается на новую категорию")
                .isEqualTo(categoryId);
    }
}
