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
 * Тема 19, задание 07. TransactionTemplate: программная граница транзакции.
 * Коммит происходит ВНУТРИ шаблона — до продолжения работы метода;
 * последующее исключение уже нечему откатывать.
 *
 * Контракт задания (см. tasks/19-transactions/README.md):
 * сервис com.example.jpalab.service.ProductService (бин "productService"),
 * метод
 *   void transferStockCommittedFirst(Long fromId, Long toId, int amount).
 * Поведение: БЕЗ @Transactional на самом методе — перенос склада
 * (снять у источника, прибавить получателю, flush) выполнить в собственной
 * транзакции через TransactionTemplate (создаётся самим из
 * PlatformTransactionManager); после commit шаблона выбросить
 * IllegalStateException (сбой внешнего системного шага).
 *
 * Тест НЕ транзакционный: прямой вызов метода. До реализации тест красный:
 * у сервиса нет метода.
 */
@SpringBootTest
class TASK07Test {

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
    void programmaticTemplateCommitsBeforeFailure() {
        Product from = seedProduct("T19-07 From", new BigDecimal("10.00"), 10);
        Product to = seedProduct("T19-07 To", new BigDecimal("20.00"), 5);

        Throwable thrown = catchThrowable(() -> TaskTestSupport.invoke(service(), "transferStockCommittedFirst",
                new Class<?>[]{Long.class, Long.class, int.class}, from.getId(), to.getId(), 3));

        assertThat(thrown).as("после commit шаблона метод выбрасывает IllegalStateException")
                .isInstanceOf(IllegalStateException.class);
        assertThat(stockOf(from.getId())).as("перенос закоммичен ДО исключения").isEqualTo(7);
        assertThat(stockOf(to.getId())).as("получатель тоже закоммичен").isEqualTo(8);
    }
}
