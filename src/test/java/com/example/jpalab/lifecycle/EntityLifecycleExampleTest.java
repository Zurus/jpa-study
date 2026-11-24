package com.example.jpalab.lifecycle;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тема 11. Жизненный цикл сущностей JPA:
 * Transient -> Persistent -> Detached / Removed, dirty checking, save vs persist/merge.
 *
 * Состояния проверяем реальными транзакциями (TransactionTemplate):
 * Persistent существует только внутри транзакции, после commit сущность
 * становится Detached, а Dirty Checking работает при flush/commit.
 */
@SpringBootTest
class EntityLifecycleExampleTest {

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

    /** @SpringBootTest без транзакций: данные реально пишутся в БД, чистим после каждого теста. */
    @AfterEach
    void cleanDatabase() {
        inTx(() -> productRepository.deleteAll());
        inTx(() -> categoryRepository.deleteAll());
    }

    @Test
    void entityGoesThroughTransientPersistentAndDetachedStates() {
        Product product = Product.builder()
                .name("Phone A").price(new BigDecimal("100.00")).stockQuantity(1)
                .build();

        // TRANSIENT: нет id, нет строки в БД, сущность не связана с EntityManager
        assertThat(product.getId()).isNull();

        Category category = inTx(() -> categoryRepository.save(
                Category.builder().name("Gadgets").build()));

        boolean containedInTx = inTx(() -> {
            product.setCategory(category);
            em.persist(product);
            // PERSISTENT: внутри persistence context, id уже присвоен (IDENTITY)
            return em.contains(product);
        });

        assertThat(containedInTx).isTrue();
        assertThat(product.getId()).isNotNull();

        // DETACHED: транзакция завершена — сущность вне persistence context,
        // но строка осталась в БД
        assertThat(inTx(() -> em.contains(product))).isFalse();
        assertThat(inTx(() -> productRepository.findById(product.getId()).isPresent())).isTrue();
    }

    @Test
    void removeDeletesRowAfterCommit() {
        Product saved = saveProduct("Phone A", "100.00");

        // REMOVED: em.remove() + commit -> DELETE
        inTx(() -> em.remove(em.find(Product.class, saved.getId())));

        assertThat(inTx(() -> productRepository.existsById(saved.getId()))).isFalse();
    }

    @Test
    void dirtyCheckingUpdatesManagedEntityWithoutExplicitSave() {
        Product saved = saveProduct("Phone A", "100.00");

        inTx(() -> {
            Product managed = em.find(Product.class, saved.getId());
            // меняем свойства ПЕРСИСТЕНТНОЙ сущности и ничего не сохраняем явно:
            // Hibernate сам сравнит состояние при flush и сгенерирует UPDATE
            managed.setPrice(new BigDecimal("777.77"));
            managed.setStockQuantity(99);
        });

        Product reloaded = inTx(() -> productRepository.findById(saved.getId()).orElseThrow());
        assertThat(reloaded.getPrice()).isEqualByComparingTo("777.77");
        assertThat(reloaded.getStockQuantity()).isEqualTo(99);
    }

    @Test
    void mergeReattachesDetachedEntityAndAppliesChanges() {
        Product saved = saveProduct("Phone A", "100.00");
        // после commit saved находится в состоянии DETACHED
        saved.setPrice(new BigDecimal("888.88"));

        Long mergedId = inTx(() -> {
            Product merged = em.merge(saved);
            // merge возвращает НОВЫЙ управляемый экземпляр
            assertThat(merged).isNotSameAs(saved);
            return merged.getId();
        });

        // id сохраняется, изменения применены к строке в БД
        assertThat(mergedId).isEqualTo(saved.getId());
        assertThat(inTx(() -> productRepository.findById(saved.getId()).orElseThrow().getPrice()))
                .isEqualByComparingTo("888.88");
    }

    @Test
    void savePersistsNewInstanceAndMergesDetachedInstance() {
        Product transientInstance = Product.builder()
                .name("Tablet C").price(new BigDecimal("100.00")).stockQuantity(1)
                .category(inTx(() -> categoryRepository.save(
                        Category.builder().name("Tablets").build())))
                .build();

        // save() на НОВОМ экземпляре — внутри вызывает persist()
        Product persisted = inTx(() -> productRepository.save(transientInstance));
        assertThat(persisted.getId()).isNotNull();
        assertThat(inTx(() -> productRepository.existsById(persisted.getId()))).isTrue();

        // save() на ИЗМЕНЕННОМ detached-экземпляре — внутри вызывает merge(),
        // id не меняется
        persisted.setPrice(new BigDecimal("300.00"));
        inTx(() -> productRepository.save(persisted));
        assertThat(inTx(() -> productRepository.findById(persisted.getId()).orElseThrow().getPrice()))
                .isEqualByComparingTo("300.00");
    }

    @Test
    void persistRejectsDetachedInstance() {
        Product saved = saveProduct("Phone A", "100.00");

        // persist() предназначен только для transient-сущностей
        assertThatThrownBy(() -> inTx(() -> {
            em.persist(saved);
            return null;
        })).isInstanceOf(EntityExistsException.class);

        // транзакция откатилась, строка в БД не пострадала
        assertThat(inTx(() -> productRepository.findById(saved.getId()).isPresent())).isTrue();
    }
}
