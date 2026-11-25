package com.example.jpalab.tasks.t11;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
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
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 11, задание 07. Identity map — свойство PERSISTENCE CONTEXT
 * (транзакции), а не «глобальный кэш»: в ДВУХ разных транзакциях
 * em.find(...) по одному id возвращают РАЗНЫЕ инстанции (a != b),
 * даже если данные идентичны.
 *
 * Контракт: сервис ProductLifecycleService, метод
 *   boolean sameInstanceAcrossTransactions(Long id)
 * (инстанция в первой REQUIRES_NEW-транзакции против инстанции во второй).
 */
@SpringBootTest
class TASK07Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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

    private <T> T inTx(Supplier<T> work) {
        return tx.execute(status -> work.get());
    }

    private Object lifecycleService() {
        try {
            return applicationContext.getBean("productLifecycleService");
        } catch (Exception e) {
            throw new AssertionError(
                    "Задание не реализовано: в контексте Spring нет бина \"productLifecycleService\"."
                            + " Убедитесь, что класс com.example.jpalab.service.ProductLifecycleService (@Service)"
                            + " существует, и добавьте в него метод по условию задания (см. tasks/11-lifecycle).", e);
        }
    }

    private Long createCommittedProduct(String name, String price, int stock) {
        Long[] ids = new Long[1];
        tx.executeWithoutResult(status -> ids[0] = productRepository.save(Product.builder()
                .name(name).price(new BigDecimal(price)).stockQuantity(stock).build()).getId());
        return ids[0];
    }

    @AfterEach
    void cleanup() {
        tx.executeWithoutResult(status -> {
            productRepository.deleteAll();
            categoryRepository.deleteAll();
        });
    }

    @Test
    void differentTransactionsDifferentInstances() {
        Long id = createCommittedProduct("T11-07 Hub", "25.00", 9);

        boolean same = TaskTestSupport.invoke(lifecycleService(), "sameInstanceAcrossTransactions",
                new Class<?>[]{Long.class}, id);

        assertThat(same)
                .as("две разные транзакции — два persistence context — две разные инстанции (a != b)")
                .isFalse();
    }
}
