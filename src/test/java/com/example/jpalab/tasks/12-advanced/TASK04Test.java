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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 12, задание 04. Auditing: @LastModifiedDate обновляется ТОЛЬКО
 * когда в БД реально уходит UPDATE.
 *
 * Наблюдение над существующими полями Product (createdAt/updatedAt +
 * AuditingEntityListener).
 * Пиновое свойство (не в AdvancedEntityExampleTest): "пустой" flush
 * (загрузили сущность, ничего не поменяли, вызвали em.flush) НЕ
 * двигает updated_at и НЕ поднимает version.
 *
 * "Пустой" прогон выполняет ВАШ метод `void loadAndFlushProduct(Long id)`
 * в `ProductVersioningService` (em.find + em.flush, без изменений) — до
 * его реализации тест красный.
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

    private Object versioningService() {
        try {
            return applicationContext.getBean("productVersioningService");
        } catch (Exception e) {
            throw new AssertionError(
                    "Задание не реализовано: в контексте Spring нет бина \"productVersioningService\". "
                            + "Создайте класс com.example.jpalab.service.ProductVersioningService (@Service) "
                            + "и реализуйте в нём метод loadAndFlushProduct(Long) "
                            + "по условию задания (см. tasks/12-advanced/README.md).", e);
        }
    }

    private void inTx(Runnable work) {
        tx.executeWithoutResult(status -> work.run());
    }

    private LocalDateTime updatedAt(Long id) {
        Timestamp raw = inTx(() -> (Timestamp) em.createNativeQuery(
                        "select updated_at from products where id = :id")
                .setParameter("id", id)
                .getSingleResult());
        return raw.toLocalDateTime();
    }

    private long version(Long id) {
        return inTx(() -> ((Number) em.createNativeQuery(
                        "select version from products where id = :id")
                .setParameter("id", id)
                .getSingleResult())).longValue();
    }

    @AfterEach
    void cleanup() {
        tx.executeWithoutResult(status -> {
            productRepository.deleteAll();
            categoryRepository.deleteAll();
        });
    }

    @Test
    void noOpFlushDoesNotBumpUpdatedAt() {
        Long id = inTx(() -> productRepository.save(Product.builder()
                .name("T12-04 Camera")
                .price(new BigDecimal("300.00"))
                .stockQuantity(1)
                .build()).getId());

        LocalDateTime before = updatedAt(id);
        assertThat(before).as("updatedAt заполнен при INSERT").isNotNull();

        // Загружаем managed-сущность, НЕ меняем ничего, принудительно flush
        // (внутри ВАШЕГО метода). Hibernate не увидит dirty-сущностей
        // и UPDATE в БД не уйдёт.
        TaskTestSupport.invoke(versioningService(), "loadAndFlushProduct",
                new Class<?>[]{Long.class}, id);

        LocalDateTime after = updatedAt(id);
        assertThat(after)
                .as("@LastModifiedDate не меняется, если UPDATE не выполнялся")
                .isEqualTo(before);
        assertThat(version(id))
                .as("без UPDATE version не поднимается")
                .isEqualTo(0L);
    }
}
