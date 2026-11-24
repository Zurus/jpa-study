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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 06, задание 01. Первый OneToMany: каскад сохранения коллекции.
 *
 * Сохраняем ТОЛЬКО категорию (save один раз) — cascade ALL включает
 * PERSIST/MERGE, и Hibernate сохранит и товары коллекции:
 * в SQL-логе 1 insert into categories + 2 insert into products.
 */
@DataJpaTest
class TASK01Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void cascadePersistsCollection() {
        Category category = Category.builder().name("CascadeCat").build();
        Product p1 = Product.builder().name("P1").price(new BigDecimal("1.00")).stockQuantity(1).build();
        Product p2 = Product.builder().name("P2").price(new BigDecimal("2.00")).stockQuantity(1).build();
        // двунаправленная связь: обе стороны
        category.getProducts().add(p1);
        p1.setCategory(category);
        category.getProducts().add(p2);
        p2.setCategory(category);

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        // сохранена только категория
        categoryRepository.save(category);
        em.flush();

        // 1 insert into categories + 2 insert into products
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);
        assertThat(statistics.getEntityInsertCount()).isEqualTo(3);

        // в БД 2 строки товаров с category_id = id категории
        Long total = (Long) em.createNativeQuery("SELECT COUNT(*) FROM products", Long.class)
                .getSingleResult();
        assertThat(total).isEqualTo(2L);
        Long inCategory = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM products WHERE category_id = ?", Long.class)
                .setParameter(1, category.getId())
                .getSingleResult();
        assertThat(inCategory).isEqualTo(2L);
        // и по-прежнему ровно одна категория
        List<Category> all = categoryRepository.findAll();
        assertThat(all).hasSize(1);
    }
}
