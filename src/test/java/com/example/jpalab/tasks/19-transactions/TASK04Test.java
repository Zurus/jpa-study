package com.example.jpalab.tasks.t19;

import com.example.jpalab.entity.Category;
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
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Тема 19, задание 04. MANDATORY: метод обязан выполняться ТОЛЬКО внутри
 * транзакции вызывающего; без транзакции прокси бросает
 * IllegalTransactionStateException, и тело метода не выполняется.
 *
 * Контракт задания (см. tasks/19-transactions/README.md):
 * сервис com.example.jpalab.service.ProductService (бин "productService"),
 * метод
 *   void moveProductToCategory(Long productId, String categoryName).
 * Поведение: установить товару категорию с заданным именем (категория
 * уже существует в БД).
 *
 * Тест НЕ транзакционный: (a) прямой вызов — без транзакции; (b) вызов
 * внутри TransactionTemplate — с транзакцией. До реализации тест красный:
 * у сервиса нет метода.
 */
@SpringBootTest
class TASK04Test {

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

    private long countCategories(String name) {
        return tx.execute(status -> (Long) em.createNativeQuery(
                        "select count(*) from categories where name = :n")
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
    void mandatoryRequiresCallerTransaction() {
        Product p = seedProduct("T19-04 Product", new BigDecimal("10.00"), 1);
        Category catB = tx.execute(status -> {
            Category c = new Category();
            c.setName("T19-04 CatB");
            return categoryRepository.save(c);
        });

        // (a) без транзакции — прокси не пускает метод в тело
        Throwable thrown = catchThrowable(() -> TaskTestSupport.invoke(service(), "moveProductToCategory",
                new Class<?>[]{Long.class, String.class}, p.getId(), "T19-04 NeverCreated"));

        assertThat(thrown).as("MANDATORY без транзакции — IllegalTransactionStateException")
                .isInstanceOf(IllegalTransactionStateException.class);
        assertThat(countCategories("T19-04 NeverCreated")).as("тело не выполнялось — категория не создана")
                .isZero();
        assertThat(categoryIdOfProduct(p.getId())).as("категория товара не изменилась (NULL)")
                .isNull();

        // (b) внутри транзакции вызывающего — метод работает и присоединяется к ней
        tx.executeWithoutResult(status -> TaskTestSupport.invoke(service(), "moveProductToCategory",
                new Class<?>[]{Long.class, String.class}, p.getId(), "T19-04 CatB"));

        assertThat(categoryIdOfProduct(p.getId())).as("категория установлена и закоммичена")
                .isEqualTo(catB.getId());
    }
}
