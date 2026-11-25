package com.example.jpalab.tasks.t11;

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
 * Тема 11, задание 09. Rollback: em.persist в транзакции с IDENTITY-
 * генератором сразу выполняет INSERT и проставляет id — но после
 * ROLLBACK строка в БД исчезает, а id при этом «существует» в памяти.
 * Классическая ловушка: не-null id ≠ «сохранено в БД».
 *
 * Контракт: сервис ProductLifecycleService, метод
 *   Long[] rollbackInsertProduct(String name, BigDecimal price, int stockQuantity)
 * (REQUIRES_NEW: persist, затем setRollbackOnly; возвращает [id]).
 */
@SpringBootTest
class TASK09Test {

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

    private long countByName(String name) {
        return inTx(() -> (Long) em.createNativeQuery(
                        "select count(*) from products where name = :n")
                .setParameter("n", name)
                .getSingleResult());
    }

    @AfterEach
    void cleanup() {
        tx.executeWithoutResult(status -> {
            productRepository.deleteAll();
            categoryRepository.deleteAll();
        });
    }

    @Test
    void rollbackDiscardsIdentityInsert() {
        String name = "T11-09 Dock";
        BigDecimal price = new BigDecimal("450.00");
        int stock = 1;

        assertThat(countByName(name)).as("до вызова строки нет").isZero();

        Long[] result = TaskTestSupport.invoke(lifecycleService(), "rollbackInsertProduct",
                new Class<?>[]{String.class, BigDecimal.class, int.class}, name, price, stock);

        assertThat(result[0])
                .as("id был присвоен при persist (IDENTITY), хотя транзакция откатилась")
                .isNotNull();
        assertThat(countByName(name))
                .as("после rollback INSERT отменён — строки в БД нет")
                .isZero();
    }
}
