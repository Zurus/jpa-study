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
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 12, задание 10. Финал: полный прогон "битвы цен" одним методом
 * сервиса — Bean Validation + Auditing + @Version в одном сценарии.
 *
 * Контракт (tasks/12-advanced/README.md):
 *   Map&lt;String, Object&gt; fullVersioningWalk(String name, BigDecimal price),
 *   каждый шаг — своя REQUIRES_NEW-транзакция:
 *   1) persist продукта (price = аргумент);
 *   2) проверка аудита: createdAt и updatedAt не null;
 *   3) "читатель" забирает копию (version = 0);
 *   4) "победитель": price + 10.00, commit (version 0 -> 1);
 *   5) "проигравший" (старая копия): price + 20.00 через
 *      productRepository.save — ловит OptimisticLockingFailureException;
 *   6) retry: price + 30.00, commit (version 1 -> 2);
 *   7) validation: managed-копии ставим price = -1.00, em.flush —
 *      ловит ConstraintViolationException (цена в БД не меняется);
 *   8) итоговое чтение состояния.
 * Возврат — Map с ключами: id (Long), auditingSet (Boolean),
 * versionAfterRace (Long), loserCaught (Boolean), finalPrice (BigDecimal),
 * finalVersion (Long), validationCaught (Boolean), updatedAtMoved (Boolean).
 *
 * До реализации тест красный: бина "productVersioningService" нет.
 */
@SpringBootTest
class TASK10Test {

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

    private Object versioningService() {
        try {
            return applicationContext.getBean("productVersioningService");
        } catch (Exception e) {
            throw new AssertionError(
                    "Задание не реализовано: в контексте Spring нет бина \"productVersioningService\". "
                            + "Создайте класс com.example.jpalab.service.ProductVersioningService (@Service) "
                            + "и реализуйте в нём метод fullVersioningWalk(String, BigDecimal) "
                            + "по условию задания (см. tasks/12-advanced/README.md).", e);
        }
    }

    @AfterEach
    void cleanup() {
        tx.executeWithoutResult(status -> {
            productRepository.deleteAll();
            categoryRepository.deleteAll();
        });
    }

    @Test
    void fullVersioningWalk() {
        String name = "T12-10 Tablet";
        BigDecimal basePrice = new BigDecimal("700.00");

        Map<String, Object> result = TaskTestSupport.invoke(versioningService(), "fullVersioningWalk",
                new Class<?>[]{String.class, BigDecimal.class}, name, basePrice);

        assertThat(result).as("метод возвращает Map<String, Object>").isNotNull();
        assertThat(result).containsKeys(
                "id", "auditingSet", "versionAfterRace", "loserCaught",
                "finalPrice", "finalVersion", "validationCaught", "updatedAtMoved");

        long id = ((Number) result.get("id")).longValue();
        assertThat(id).as("id продукта").isPositive();
        assertThat(result.get("auditingSet"))
                .as("@CreatedDate/@LastModifiedDate заполнены при INSERT").isEqualTo(Boolean.TRUE);
        assertThat(((Number) result.get("versionAfterRace")).longValue())
                .as("version после 'гонки': победитель поднял 0 -> 1").isEqualTo(1L);
        assertThat(result.get("loserCaught"))
                .as("проигравший поймал OptimisticLockingFailureException").isEqualTo(Boolean.TRUE);
        assertThat(new BigDecimal(result.get("finalPrice").toString()))
                .as("финальная цена = base + 30.00 (retry-обновление)").isEqualByComparingTo("730.00");
        assertThat(((Number) result.get("finalVersion")).longValue())
                .as("финальный version: победитель + retry").isEqualTo(2L);
        assertThat(result.get("validationCaught"))
                .as("отрицательная цена отклонена Bean Validation на flush").isEqualTo(Boolean.TRUE);
        assertThat(result.get("updatedAtMoved"))
                .as("@LastModifiedDate продвинулся после реального обновления").isEqualTo(Boolean.TRUE);

        // Независимая проверка состояния БД.
        long count = inTx(() -> (Long) em.createNativeQuery(
                        "select count(*) from products where name = :n")
                .setParameter("n", name)
                .getSingleResult());
        assertThat(count).as("ровно одна строка с именем из аргумента").isEqualTo(1);

        Object[] row = inTx(() -> (Object[]) em.createNativeQuery(
                        "select price, version, created_at from products where id = :id")
                .setParameter("id", id)
                .getSingleResult());
        assertThat(new BigDecimal(row[0].toString())).isEqualByComparingTo("730.00");
        assertThat(((Number) row[1]).longValue()).isEqualTo(2L);
        assertThat(row[2]).as("created_at в БД").isNotNull();
    }
}
