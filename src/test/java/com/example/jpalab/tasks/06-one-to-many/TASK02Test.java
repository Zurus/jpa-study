package com.example.jpalab.tasks.t06;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.service.ProductLifecycleService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 06, задание 02. Добавление в коллекцию managed-родителя.
 *
 * Коллекция managed-сущности «под контролем»: Hibernate видит
 * добавленный элемент при flush и уводит его в БД через cascade PERSIST —
 * один insert into products с FK, без пересоздания категории.
 *
 * Добавление товара выполняет ВАШ метод сервиса
 * `void addProductToCategory(Long categoryId, String productName,
 * BigDecimal price, int stockQuantity)` в `ProductLifecycleService`
 * (загружает managed-категорию, создаёт товар, выставляет обе стороны
 * связи, flush) — до его реализации тест красный.
 */
@DataJpaTest
@Import(ProductLifecycleService.class)
class TASK02Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductLifecycleService lifecycleService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void addToManagedCollection() {
        // пустая категория
        Category saved = categoryRepository.saveAndFlush(
                Category.builder().name("EmptyCat").build());
        em.clear();

        // загружаем managed-родителя
        Category managed = categoryRepository.findById(saved.getId()).orElseThrow();
        assertThat(managed.getProducts()).isEmpty();

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        // добавление товара — ВАШИМ методом (создаёт товар с обратной ссылкой
        // и добавляет в коллекцию managed-категории)
        TaskTestSupport.invoke(lifecycleService, "addProductToCategory",
                new Class<?>[]{Long.class, String.class, BigDecimal.class, int.class},
                saved.getId(), "NewP", new BigDecimal("5.00"), 1);

        em.flush();

        // ровно один insert into products (с FK); категория не пересоздавалась
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
        assertThat(statistics.getEntityInsertCount()).isEqualTo(1);
        assertThat(statistics.getEntityUpdateCount()).isZero();

        Long count = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM products WHERE category_id = ?", Long.class)
                .setParameter(1, saved.getId())
                .getSingleResult();
        assertThat(count).isEqualTo(1L);
        assertThat(managed.getProducts()).hasSize(1);
    }
}
