package com.example.jpalab.lifecycle;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тема 12. Продвинутые механизмы JPA: Bean Validation, Auditing,
 * @Version и оптимистичная блокировка.
 *
 * Реальные транзакции через TransactionTemplate: validation срабатывает
 * на persist, auditing — на persist/flush, @Version — на commit с UPDATE.
 */
@SpringBootTest
class AdvancedEntityExampleTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TransactionTemplate txTemplate;

    @TestConfiguration
    static class TestBeans {
        @Bean
        TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
            return new TransactionTemplate(transactionManager);
        }
    }

    private <T> T inTx(Supplier<T> work) {
        return txTemplate.execute(status -> work.get());
    }

    private void inTx(Runnable work) {
        txTemplate.executeWithoutResult(status -> work.run());
    }

    private Product saveProduct(String name, String price) {
        return inTx(() -> {
            Category category = categoryRepository.save(
                    Category.builder().name(name + " category").build());
            return productRepository.save(Product.builder()
                    .name(name)
                    .price(new BigDecimal(price))
                    .stockQuantity(1)
                    .category(category)
                    .build());
        });
    }

    @AfterEach
    void cleanDatabase() {
        inTx(() -> productRepository.deleteAll());
        inTx(() -> categoryRepository.deleteAll());
    }

    @Test
    void beanValidationRejectsInvalidEntityOnPersist() {
        assertThatThrownBy(() -> inTx(() -> {
            // @NotBlank на Product.name — нарушение при persist
            em.persist(Product.builder()
                    .name(null)
                    .price(new BigDecimal("100.00"))
                    .stockQuantity(1)
                    .build());
            return null;
        }))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("name");

        // транзакция откатилась — ничего не записано
        assertThat(inTx(() -> productRepository.count())).isZero();
    }

    @Test
    void auditingFillsCreatedAtAndUpdatedAtAutomatically() throws InterruptedException {
        Product saved = saveProduct("Phone A", "100.00");

        // @CreatedDate/@LastModifiedDate + @EnableJpaAuditing:
        // поля заполняются фреймворком без нашего кода
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isAfterOrEqualTo(saved.getCreatedAt());

        Thread.sleep(200); // гарантируем различимые таймстампы (микро-секундная точность H2)
        LocalDateTime updatedAtBefore = saved.getUpdatedAt();

        inTx(() -> em.find(Product.class, saved.getId()).setPrice(new BigDecimal("150.00")));

        Product reloaded = inTx(() -> productRepository.findById(saved.getId()).orElseThrow());
        // @CreatedDate (updatable = false) — не изменилось (H2 хранит TIMESTAMP(6),
        // поэтому сравниваем с допуском в несколько миллисекунд)
        assertThat(Duration.between(reloaded.getCreatedAt(), saved.getCreatedAt()).abs())
                .isLessThan(Duration.ofMillis(2));
        // @LastModifiedDate — автоматически обновлено при изменении
        assertThat(reloaded.getUpdatedAt()).isAfter(updatedAtBefore);
    }

    @Test
    void versionStartsAtZeroAndIncrementsOnEachUpdate() {
        Product saved = saveProduct("Phone A", "100.00");
        assertThat(saved.getVersion()).isZero();

        inTx(() -> em.find(Product.class, saved.getId()).setPrice(new BigDecimal("150.00")));
        Product afterFirst = inTx(() -> productRepository.findById(saved.getId()).orElseThrow());
        assertThat(afterFirst.getVersion()).isEqualTo(1L);

        inTx(() -> em.find(Product.class, afterFirst.getId()).setPrice(new BigDecimal("175.00")));
        Product afterSecond = inTx(() -> productRepository.findById(afterFirst.getId()).orElseThrow());
        assertThat(afterSecond.getVersion()).isEqualTo(2L);
    }

    @Test
    void optimisticLockingRejectsStaleUpdate() {
        Product saved = saveProduct("Phone A", "100.00");

        // "Читатель" загрузил копию (version = 0) и продолжил работу с ней...
        Product staleCopy = inTx(() -> em.find(Product.class, saved.getId()));

        // ...а "параллельная" транзакция успела обновить строку (version 0 -> 1)
        inTx(() -> em.find(Product.class, saved.getId()).setPrice(new BigDecimal("200.00")));

        // Старая копия пытается сохранить: UPDATE ... WHERE version = 0
        // не находит ни одной строки -> OptimisticLockException
        staleCopy.setPrice(new BigDecimal("999.99"));
        assertThatThrownBy(() -> inTx(() -> {
            em.merge(staleCopy);
            return null;
        })).isInstanceOf(OptimisticLockException.class);

        // В БД осталось значение "победившего" обновления
        Product winner = inTx(() -> productRepository.findById(saved.getId()).orElseThrow());
        assertThat(winner.getPrice()).isEqualByComparingTo("200.00");
        assertThat(winner.getVersion()).isEqualTo(1L);
    }
}
