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
 * Тема 19, задание 05. REQUIRED (дефолт): если транзакция уже открыта,
 * метод присоединяется к ней и НЕ коммитится сам — изменение живёт и
 * умирает вместе с транзакцией вызывающего (контраст к заданию 03).
 *
 * Контракт задания (см. tasks/19-transactions/README.md):
 * сервис com.example.jpalab.service.ProductService (бин "productService"),
 * метод
 *   void updateProductPrice(Long id, BigDecimal newPrice).
 * Поведение: изменить цену товара (стандартная транзакция, дефолтный
 * уровень propagation).
 *
 * Тест НЕ транзакционный: вызов метода — внутри TransactionTemplate,
 * затем сбой внешней транзакции. До реализации тест красный: у сервиса
 * нет метода.
 */
@SpringBootTest
class TASK05Test {

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
    void requiredJoinsCallerAndDiesWithIt() {
        Product p = seedProduct("T19-05 Product", new BigDecimal("10.00"), 7);

        RuntimeException outerFailure = catchThrowableOfType(() -> tx.execute(status -> {
            TaskTestSupport.invoke(service(), "updateProductPrice",
                    new Class<?>[]{Long.class, BigDecimal.class}, p.getId(), new BigDecimal("77.77"));
            throw new IllegalStateException("сбой внешней транзакции");
        }), IllegalStateException.class);

        assertThat(outerFailure).as("внешняя транзакция оборвана исключением").isNotNull();
        assertThat(priceOf(p.getId()))
                .as("REQUIRED: метод присоединился к транзакции вызывающего — изменение откатилось вместе с ней")
                .isEqualByComparingTo("10.00");
    }
}
