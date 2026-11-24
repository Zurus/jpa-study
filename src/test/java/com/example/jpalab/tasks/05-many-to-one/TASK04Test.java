package com.example.jpalab.tasks.t05;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.TransientPropertyValueException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тема 05, задание 04. Transient-категория: исключение без cascade.
 *
 * По умолчанию cascade на ManyToOne НЕ настроен: Hibernate не может угадать,
 * как сохранить «чужую» сущность — без cascade это ошибка, а не молчаливый
 * persist. В реальном приложении: сохранить категорию отдельно или добавить
 * cascade (с рисками: cascade на «одной» стороне затрагивает всех владельцев).
 *
 * Фактическое поведение (Hibernate 6 + IDENTITY), зафиксировано в тесте:
 * - `persist` товара проходит: IDENTITY сразу выполняет INSERT (строка
 *   с category_id = NULL появляется ВНУТРИ транзакции) — это и есть
 *   главный недостаток IDENTITY (инсерт в момент persist, без батчинга);
 * - `flush` бросает исключение с причиной
 *   org.hibernate.TransientPropertyValueException («object references an
 *   unsaved transient instance — save the transient instance before
 *   flushing»); на границе репозитория Spring транслирует его в
 *   org.springframework.dao.InvalidDataAccessApiUsageException;
 * - транзакция откатывается, и в закоммиченной БД строки товара НЕТ.
 *
 * Поэтому состояние «закоммиченной» БД проверяется в новой транзакции —
 * тест самодостаточен: @SpringBootTest + TransactionTemplate,
 * своя настройка и очистка.
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
    private TransactionTemplate tx;

    @TestConfiguration
    static class TestBeans {
        @Bean
        TransactionTemplate transactionTemplate(PlatformTransactionManager txm) {
            return new TransactionTemplate(txm);
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
    void transientCategoryRejected() {
        // НОВАЯ (transient) категория — без save
        Category fresh = Category.builder().name("Fresh").build();
        Product product = Product.builder()
                .name("WithTransient")
                .price(new BigDecimal("10.00"))
                .stockQuantity(1)
                .category(fresh)
                .build();

        // сохранение (persist + flush) завершается исключением;
        // транзакция откатывается
        assertThatThrownBy(() -> tx.executeWithoutResult(status ->
                productRepository.saveAndFlush(product)))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasRootCauseInstanceOf(TransientPropertyValueException.class);

        // в закоммиченной БД строки товара нет (проверяем в новой tx)
        Long count = tx.execute(status -> (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM products", Long.class)
                .getSingleResult());
        assertThat(count).isZero();
    }
}
