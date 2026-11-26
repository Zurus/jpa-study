package com.example.jpalab.tasks.t05;

import com.example.jpalab.service.ProductLifecycleService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тема 05, задание 07. FK-ограничение на уровне БД.
 *
 * Внешний ключ гарантируется БД (products.category_id → categories.id),
 * а не только маппингом: даже «правильный» JPA-маппинг нельзя обходить
 * на уровне SQL без нарушения FK. Hibernate оборачивает нарушение
 * ссылочной целостности H2 в org.hibernate.exception.ConstraintViolationException
 * (при прямой работе через EntityManager Spring не транслирует его в
 * DataIntegrityViolationException — транслирует только на границе репозитория).
 *
 * Важно: Hibernate НЕ заполняет аудитные поля при native-вставке,
 * поэтому created_at/updated_at передаются явно.
 *
 * Настоящую (реальную) категорию для успешного INSERT создаёт ВАШ метод
 * сервиса `Long createCategory(String name)` в `ProductLifecycleService`
 * (сохраняет категорию и возвращает её id) — до его реализации тест красный.
 */
@DataJpaTest
@Import(ProductLifecycleService.class)
class TASK07Test {

    @Autowired
    private ProductLifecycleService lifecycleService;

    @PersistenceContext
    private EntityManager em;

    private void nativeInsert(String name, Long categoryId) {
        em.createNativeQuery("""
                INSERT INTO products (name, price, stock_quantity, category_id, created_at, updated_at)
                VALUES (:name, :price, :stock, :categoryId, :now, :now)
                """)
                .setParameter("name", name)
                .setParameter("price", new BigDecimal("10.00"))
                .setParameter("stock", 1)
                .setParameter("categoryId", categoryId)
                .setParameter("now", LocalDateTime.now())
                .executeUpdate();
    }

    @Test
    void dbEnforcesFk() {
        // INSERT с вымышленным category_id — нарушение FK
        assertThatThrownBy(() -> nativeInsert("FakeFk", 999_999L))
                .isInstanceOf(ConstraintViolationException.class);

        // реальная категория — ВАШИМ методом; INSERT с её id — проходит
        Long categoryId = TaskTestSupport.invoke(lifecycleService, "createCategory",
                new Class<?>[]{String.class}, "Real");
        assertThat(categoryId).isNotNull();

        // INSERT с существующим id — проходит
        nativeInsert("GoodFk", categoryId);
        Long count = (Long) em.createNativeQuery("SELECT COUNT(*) FROM products", Long.class)
                .getSingleResult();
        assertThat(count).isEqualTo(1);
    }
}
