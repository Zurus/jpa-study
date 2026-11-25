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
 * Тема 11, задание 02. Состояние Persistent существует ТОЛЬКО внутри
 * транзакции: em.contains(entity) == true, пока сущность в persistence
 * context своей транзакции, и == false для ТОЙ ЖЕ инстанции в другой
 * транзакции (после commit инстанция — Detached, строка в БД осталась).
 *
 * Контракт: сервис ProductLifecycleService, метод
 *   boolean[] containsInsideAndAfterTransaction(Long id)
 * возвращает [containsInsideTx, containsAfterCommit].
 */
@SpringBootTest
class TASK02Test {

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
    void persistentStateExistsOnlyInsideTransaction() {
        Long id = createCommittedProduct("T11-02 Mouse", "50.00", 7);

        boolean[] result = TaskTestSupport.invoke(lifecycleService(), "containsInsideAndAfterTransaction",
                new Class<?>[]{Long.class}, id);

        assertThat(result).isNotNull();
        assertThat(result[0]).as("em.contains внутри транзакции, где инстанция загружена").isTrue();
        assertThat(result[1]).as("em.contains для той же инстанции в ДРУГОЙ транзакции (после commit)").isFalse();
        assertThat(inTx(() -> productRepository.existsById(id)))
                .as("сама строка после commit осталась в БД").isTrue();
    }
}
