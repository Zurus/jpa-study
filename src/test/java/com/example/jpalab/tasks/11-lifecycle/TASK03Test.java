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
 * Тема 11, задание 03. save() на НОВОМ экземпляре: JpaRepository.save
 * маршрутизирует transient-сущность в persist (INSERT, один SQL, id
 * проставляется сразу — IDENTITY).
 *
 * Контракт: сервис ProductLifecycleService, метод
 *   Object[] saveNewProduct(String name, BigDecimal price, int stockQuantity)
 * возвращает [id, inputIdSet] — id сохранённой сущности и факт, что id
 * проставлен на исходный (вошедший в save) экземпляр.
 */
@SpringBootTest
class TASK03Test {

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
    void saveRoutesNewInstanceToPersist() {
        String name = "T11-03 Keyboard";
        BigDecimal price = new BigDecimal("120.00");
        int stock = 5;

        assertThat(countByName(name)).as("до save строки нет").isZero();

        Object[] result = TaskTestSupport.invoke(lifecycleService(), "saveNewProduct",
                new Class<?>[]{String.class, BigDecimal.class, int.class}, name, price, stock);

        Long id = (Long) result[0];
        assertThat(id).as("id сохранённой сущности").isNotNull();
        assertThat((Boolean) result[1]).as("id проставлен на исходный (transient) экземпляр").isTrue();
        assertThat(countByName(name)).as("save нового экземпляра создал ровно одну строку").isEqualTo(1);

        Object[] row = rowById(id);
        assertThat((BigDecimal) row[0]).as("цена в БД").isEqualByComparingTo(price);
        assertThat(row[1]).as("version после INSERT").isEqualTo(0L);
        assertThat(row[2]).as("stock в БД").isEqualTo(5);
    }
}
