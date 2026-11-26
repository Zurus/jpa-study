package com.example.jpalab.tasks.t12;

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
 * Тема 12, задание 08. Оптимистичная блокировка + retry-паттерн:
 * метод сервиса пересохраняет сущность с перезагрузкой после конфликта.
 *
 * Контракт (tasks/12-advanced/README.md):
 *   Object[] saveWithRetry(Product product, BigDecimal targetPrice, int maxAttempts)
 *   — ПЕРВАЯ попытка сохраняет переданный экземпляр (без перезагрузки);
 *     при OptimisticLockingFailureException — перезагружает сущность
 *     (новый REQUIRES_NEW, em.find) и повторяет; attempts = число
 *     реально выполненных productRepository.save.
 *     Возврат: [0] Integer — attempts, [1] Long — итоговый version,
 *     [2] BigDecimal — итоговая цена.
 *
 * Сценарий теста: тест сам поднимает version 0 -> 1, затем передаёт
 * устаревшую (version = 0) копию — первая попытка обязана упасть,
 * вторая (после перезагрузки) — пройти.
 *
 * До реализации тест красный: бина "productVersioningService" нет.
 */
@SpringBootTest
class TASK08Test {

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

    private void inTx(Runnable work) {
        tx.executeWithoutResult(status -> work.run());
    }

    private Object versioningService() {
        try {
            return applicationContext.getBean("productVersioningService");
        } catch (Exception e) {
            throw new AssertionError(
                    "Задание не реализовано: в контексте Spring нет бина \"productVersioningService\". "
                            + "Создайте класс com.example.jpalab.service.ProductVersioningService (@Service) "
                            + "и реализуйте в нём метод saveWithRetry(Product, BigDecimal, int) "
                            + "по условию задания (см. tasks/12-advanced/README.md).", e);
        }
    }

    private Object[] rowById(Long id) {
        return inTx(() -> (Object[]) em.createNativeQuery(
                        "select price, version from products where id = :id")
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
    void saveWithRetrySucceedsOnSecondAttempt() {
        Long id = inTx(() -> productRepository.save(Product.builder()
                .name("T12-08 Monitor")
                .price(new BigDecimal("100.00"))
                .stockQuantity(1)
                .build()).getId());

        // Тест готовит конфликт: устаревшая копия (v0) + чужое обновление (v0 -> v1).
        Product stale = inTx(() -> em.find(Product.class, id));
        inTx(() -> em.find(Product.class, id).setPrice(new BigDecimal("200.00")));

        Object[] result = TaskTestSupport.invoke(versioningService(), "saveWithRetry",
                new Class<?>[]{Product.class, BigDecimal.class, int.class},
                stale, new BigDecimal("300.00"), 5);

        assertThat(result).as("метод возвращает [attempts, version, price]").hasSize(3);
        assertThat(((Number) result[0]).intValue())
                .as("попыток ровно две: первая (старая версия) отклонена, вторая — успех")
                .isEqualTo(2);
        assertThat(((Number) result[1]).longValue())
                .as("итоговый version: 1 (чужое обновление) + 1 (наш retry)").isEqualTo(2L);
        assertThat(new BigDecimal(result[2].toString()))
                .as("итоговая цена — целевая цена метода").isEqualByComparingTo("300.00");

        // Независимая проверка состояния БД.
        Object[] row = rowById(id);
        assertThat(new BigDecimal(row[0].toString())).isEqualByComparingTo("300.00");
        assertThat(((Number) row[1]).longValue()).isEqualTo(2L);
    }
}
