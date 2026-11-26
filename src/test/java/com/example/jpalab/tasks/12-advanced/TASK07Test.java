package com.example.jpalab.tasks.t12;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
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
import com.example.jpalab.tasks.TaskTestSupport;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 12, задание 07. Оптимистичная блокировка: "гонка" двух обновлений
 * через сервис (контракт — tasks/12-advanced/README.md).
 *
 * Студент создаёт com.example.jpalab.service.ProductVersioningService
 * (@Service, бин "productVersioningService") с методом
 *   Object[] loseTheRace(Long id, BigDecimal winnerPrice, BigDecimal loserPrice),
 * где каждый шаг — своя REQUIRES_NEW-транзакция:
 *   T1: find(id) — "читатель" забирает копию;
 *   T2: find(id), setPrice(winnerPrice), commit — "победитель";
 *   T3: stale.setPrice(loserPrice), productRepository.save(stale) —
 *       проигравший должен поймать OptimisticLockingFailureException.
 * Возврат: [0] Long — итоговый version, [1] BigDecimal — итоговая цена,
 * [2] Boolean — поймал ли проигравший исключение.
 *
 * До реализации тест красный: бина "productVersioningService" нет.
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

    private Object versioningService() {
        try {
            return applicationContext.getBean("productVersioningService");
        } catch (Exception e) {
            throw new AssertionError(
                    "Задание не реализовано: в контексте Spring нет бина \"productVersioningService\". "
                            + "Создайте класс com.example.jpalab.service.ProductVersioningService (@Service) "
                            + "и реализуйте в нём метод loseTheRace(Long, BigDecimal, BigDecimal) "
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
    void loseTheRace() {
        Long id = inTx(() -> productRepository.save(Product.builder()
                .name("T12-07 Headset")
                .price(new BigDecimal("100.00"))
                .stockQuantity(1)
                .build()).getId());

        Object[] result = TaskTestSupport.invoke(versioningService(), "loseTheRace",
                new Class<?>[]{Long.class, BigDecimal.class, BigDecimal.class},
                id, new BigDecimal("200.00"), new BigDecimal("999.99"));

        assertThat(result).as("метод возвращает [version, price, loserCaught]").hasSize(3);
        assertThat(((Number) result[0]).longValue())
                .as("итоговый version: победитель поднял 0 -> 1").isEqualTo(1L);
        assertThat(new BigDecimal(result[1].toString()))
                .as("итоговая цена — значение победителя").isEqualByComparingTo("200.00");
        assertThat(result[2]).as("проигравший поймал OptimisticLockingFailureException")
                .isInstanceOf(Boolean.class);
        assertThat((Boolean) result[2]).isTrue();

        // Независимая проверка состояния БД.
        Object[] row = rowById(id);
        assertThat(new BigDecimal(row[0].toString())).isEqualByComparingTo("200.00");
        assertThat(((Number) row[1]).longValue()).isEqualTo(1L);
    }
}
