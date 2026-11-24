package com.example.jpalab.tasks.t05;

import com.example.jpalab.entity.Category;
import com.example.jpalab.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
 */
@DataJpaTest
class TASK07Test {

    @Autowired
    private CategoryRepository categoryRepository;

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
        // реальный id категории
        Category category = categoryRepository.saveAndFlush(
                Category.builder().name("Real").build());

        // INSERT с вымышленным category_id — нарушение FK
        assertThatThrownBy(() -> nativeInsert("FakeFk", 999_999L))
                .isInstanceOf(ConstraintViolationException.class);

        // INSERT с существующим id — проходит
        nativeInsert("GoodFk", category.getId());
        Long count = (Long) em.createNativeQuery("SELECT COUNT(*) FROM products", Long.class)
                .getSingleResult();
        assertThat(count).isEqualTo(1);
    }
}
