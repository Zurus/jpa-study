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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тема 12, задание 06. Оптимистичная блокировка: @Version + обновление
 * по устаревшей копии через репозиторий.
 *
 * Наблюдение над существующим поведением (@Version уже на Product).
 * Пиновое свойство (в отличие от AdvancedEntityExampleTest, где em.merge
 * бросал JPA-исключение OptimisticLockException): через ГРАНИЦУ
 * Spring Data (productRepository.save) Hibernate'овский
 * StaleObjectStateException транслируется Spring'ом в
 * org.springframework.dao.OptimisticLockingFailureException.
 *
 * "Гонку" (чтение копии, обновление-победитель, сохранение устаревшей
 * копии через репозиторий) выполняет ВАШ метод
 * `String staleSaveExceptionType(Long id, BigDecimal winnerPrice,
 * BigDecimal stalePrice)` в `ProductVersioningService` — он возвращает
 * имя класса исключения, пойманного при сохранении устаревшей копии
 * (каждый шаг — отдельная транзакция). До его реализации тест красный.
 */
@SpringBootTest
class TASK06Test {

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
                            + "и реализуйте в нём метод staleSaveExceptionType(Long, BigDecimal, BigDecimal) "
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
    void staleSaveViaRepositoryThrowsOptimisticLockingFailureException() {
        Long id = inTx(() -> productRepository.save(Product.builder()
                .name("T12-06 Speaker")
                .price(new BigDecimal("100.00"))
                .stockQuantity(2)
                .build()).getId());

        // ВАШ метод: T1 — "читатель" забирает копию (version = 0);
        // T2 — "победитель" обновляет цену (version 0 -> 1);
        // T3 — сохранение устаревшей копии через productRepository.save
        // должно упасть на границе репозитория.
        String exceptionType = TaskTestSupport.invoke(versioningService(),
                "staleSaveExceptionType",
                new Class<?>[]{Long.class, BigDecimal.class, BigDecimal.class},
                id, new BigDecimal("200.00"), new BigDecimal("999.99"));

        assertThat(exceptionType)
                .as("Spring транслирует StaleObjectStateException в "
                        + "OptimisticLockingFailureException на границе репозитория")
                .isEqualTo("OptimisticLockingFailureException");

        Object[] row = rowById(id);
        assertThat(new BigDecimal(row[0].toString()))
                .as("в БД осталось значение 'победившего' обновления").isEqualByComparingTo("200.00");
        assertThat(((Number) row[1]).longValue())
                .as("version победившего обновления").isEqualTo(1L);
    }
}
