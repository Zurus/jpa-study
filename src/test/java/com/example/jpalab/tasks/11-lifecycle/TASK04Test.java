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
 * Тема 11, задание 04. merge() на TRANSIENT-инстанции (id = null):
 * Hibernate копирует состояние в НОВУЮ managed-инстанцию и выполняет
 * INSERT; исходный transient-экземпляр остаётся вне persistence context
 * (его id так и не проставляется).
 *
 * Контракт: сервис ProductLifecycleService, метод
 *   Object[] mergeTransientProduct(String name, BigDecimal price, int stockQuantity)
 * возвращает [managedId, managedIsNotSameAsInput, inputIdRemainsNull].
 */
@SpringBootTest
class TASK04Test {

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

    private Object[] rowById(Long id) {
        return inTx(() -> (Object[]) em.createNativeQuery(
                        "select price, version, stock_quantity from products where id = :id")
                .setParameter("id", id)
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
    void mergeOfTransientInsertsNewRow() {
        String name = "T11-04 Monitor";
        BigDecimal price = new BigDecimal("900.00");
        int stock = 2;

        assertThat(countByName(name)).as("до merge строки нет").isZero();

        Object[] result = TaskTestSupport.invoke(lifecycleService(), "mergeTransientProduct",
                new Class<?>[]{String.class, BigDecimal.class, int.class}, name, price, stock);

        Long managedId = (Long) result[0];
        assertThat(managedId).as("id новой managed-инстанции").isNotNull();
        assertThat((Boolean) result[1]).as("merge вернул НОВУЮ инстанцию (isNotSameAs исходной)").isTrue();
        assertThat((Boolean) result[2]).as("у исходной transient-инстанции id остался null").isTrue();
        assertThat(countByName(name)).as("merge transient-экземпляра выполнил INSERT — ровно одна строка").isEqualTo(1);

        Object[] row = rowById(managedId);
        assertThat((BigDecimal) row[0]).as("цена в БД").isEqualByComparingTo(price);
        assertThat(row[1]).as("version после INSERT").isEqualTo(0L);
        assertThat(row[2]).as("stock в БД").isEqualTo(2);
    }
}
