package com.example.jpalab.tasks.t06;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 06, задание 02. Добавление в коллекцию managed-родителя.
 *
 * Коллекция managed-сущности «под контролем»: Hibernate видит
 * добавленный элемент при flush и уводит его в БД через cascade PERSIST —
 * один insert into products с FK, без пересоздания категории.
 */
@DataJpaTest
class TASK02Test {

    @Autowired
    private CategoryRepository categoryRepository;

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

        Product newProduct = Product.builder()
                .name("NewP")
                .price(new BigDecimal("5.00"))
                .stockQuantity(1)
                .build();
        // обратная ссылка обязательна
        newProduct.setCategory(managed);
        managed.getProducts().add(newProduct);

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

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
