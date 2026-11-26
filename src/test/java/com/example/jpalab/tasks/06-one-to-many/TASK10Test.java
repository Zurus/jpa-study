package com.example.jpalab.tasks.t06;

import com.example.jpalab.entity.Category;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.service.ProductLifecycleService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
 * Тема 06, задание 10. Откат транзакции: коллекция и строки отменяются.
 *
 * flush != commit: em.flush() лишь «выталкивает» SQL в транзакцию, а
 * rollback отменяет ВСЁ, включая flushed-INSERT-ы. Внутренний flush
 * ничего не фиксирует, если транзакция завершена откатом.
 *
 * Добавление двух товаров в managed-категорию выполняет ВАШ метод сервиса
 * `void addTwoProductsToCategory(Long categoryId)` в `ProductLifecycleService`
 * (загружает managed-категорию, добавляет 2 товара с обратными ссылками,
 * flush) — до его реализации тест красный.
 */
@SpringBootTest
class TASK10Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductLifecycleService lifecycleService;

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

    @Test
    void rollbackUndoesCollectionInserts() {
        // setup tx: пустая категория (закоммичена)
        Long categoryId = inTx(() -> categoryRepository.save(
                Category.builder().name("RollbackCat").build()).getId());

        // tx: 2 товара в коллекцию managed-категории — ВАШИМ методом,
        // flush внутри транзакции, затем rollback
        assertThatThrownBy(() -> inTx(() -> {
            TaskTestSupport.invoke(lifecycleService, "addTwoProductsToCategory",
                    new Class<?>[]{Long.class}, categoryId);
            em.flush(); // INSERT-ы выполнены В РАМКАХ транзакции
            throw new RuntimeException("rollback for test");
        })).isInstanceOf(RuntimeException.class);

        // новая tx: товаров в БД нет, свежая коллекция пуста
        inTx(() -> {
            Long count = (Long) em.createNativeQuery(
                    "SELECT COUNT(*) FROM products WHERE category_id = ?", Long.class)
                    .setParameter(1, categoryId)
                    .getSingleResult();
            assertThat(count).isZero();
            Category fresh = categoryRepository.findById(categoryId).orElseThrow();
            assertThat(fresh.getProducts()).isEmpty();
            return null;
        });
    }
}
