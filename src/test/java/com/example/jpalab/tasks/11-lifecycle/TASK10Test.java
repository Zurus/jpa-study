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
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 11, задание 10. Финальный прогон всего жизненного цикла в одном
 * сервисном методе (каждый шаг — своя REQUIRES_NEW-транзакция):
 * transient → persistent (persist) → dirty checking (без save, version +1)
 * → detached-изменение БЕЗ merge (теряется) → removed (em.remove).
 *
 * Контракт: сервис ProductLifecycleService, метод
 *   Map&lt;String, Object&gt; fullLifecycleWalk(String name, BigDecimal price, int stockQuantity)
 * возвращает:
 *   "id" — Long, id созданной сущности;
 *   "versionAfterDirty" — Long, version после dirty-обновления (ожид. 1);
 *   "priceAfterDetachedMutation" — BigDecimal, цена в БД после изменения
 *       DETACHED-инстанции без merge (ожид. price + 10.00 — изменение
 *       из шага 2 применено, из шага 3 — потеряно);
 *   "existsAfterRemove" — Boolean, есть ли строка в БД после em.remove;
 *   "findableAfterRemove" — Boolean, находит ли em.find сущность после
 *       commit удаления.
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
    @SuppressWarnings("unchecked")
    void fullLifecycleWalk() {
        String name = "T11-10 Tablet";
        BigDecimal price = new BigDecimal("700.00");
        int stock = 6;

        Map<String, Object> result = TaskTestSupport.invoke(lifecycleService(), "fullLifecycleWalk",
                new Class<?>[]{String.class, BigDecimal.class, int.class}, name, price, stock);

        Long id = (Long) result.get("id");
        assertThat(id).as("id созданной сущности").isNotNull();
        assertThat(result.get("versionAfterDirty"))
                .as("version после dirty-обновления (без save)").isEqualTo(1L);
        BigDecimal dbPrice = (BigDecimal) result.get("priceAfterDetachedMutation");
        assertThat(dbPrice)
                .as("цена в БД: dirty-изменение (+10) применено, detached-изменение (+20) потеряно")
                .isEqualByComparingTo(price.add(new BigDecimal("10.00")));
        assertThat(result.get("existsAfterRemove")).as("после em.remove + commit строки нет").isEqualTo(false);
        assertThat(result.get("findableAfterRemove")).as("после commit удаления em.find не находит сущность").isEqualTo(false);
        assertThat(countByName(name)).as("в БД после полного цикла строк с таким именем нет").isZero();
    }
}
