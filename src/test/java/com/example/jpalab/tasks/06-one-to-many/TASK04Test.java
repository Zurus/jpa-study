package com.example.jpalab.tasks.t06;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
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
 * Тема 06, задание 04. Перенос товара между категориями.
 *
 * «Перенос» — это смена FK, а не пересоздание строки: коллекция — лишь
 * навигация, реальное изменение — колонка category_id на стороне товара
 * (владелец FK — ManyToOne-сторона). В SQL-логе ровно один
 * update products set ... category_id = ?, без delete/insert.
 */
@DataJpaTest
class TASK04Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void moveBetweenCategories() {
        Category a = categoryRepository.saveAndFlush(Category.builder().name("From").build());
        Category b = categoryRepository.saveAndFlush(Category.builder().name("To").build());
        Product product = Product.builder()
                .name("Mover").price(new BigDecimal("9.00")).stockQuantity(1).category(a).build();
        productRepository.saveAndFlush(product);
        em.clear();

        Category managedA = categoryRepository.findById(a.getId()).orElseThrow();
        Category managedB = categoryRepository.findById(b.getId()).orElseThrow();
        Product managedProduct = (Product) managedA.getProducts().get(0);

        // перенос: убрать из A, добавить в B, обновить обратную ссылку
        managedA.getProducts().remove(managedProduct);
        managedB.getProducts().add(managedProduct);
        managedProduct.setCategory(managedB);

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        em.flush();

        // ровно один UPDATE FK; НЕТ delete/insert по товару
        assertThat(statistics.getEntityUpdateCount()).isEqualTo(1);
        assertThat(statistics.getEntityInsertCount()).isZero();
        assertThat(statistics.getEntityDeleteCount()).isZero();

        // товар всё тот же (id не изменился), строка одна
        Object fk = em.createNativeQuery(
                "SELECT category_id FROM products WHERE id = ?", Long.class)
                .setParameter(1, managedProduct.getId())
                .getSingleResult();
        assertThat(fk).isEqualTo(b.getId());
        Long total = (Long) em.createNativeQuery("SELECT COUNT(*) FROM products", Long.class)
                .getSingleResult();
        assertThat(total).isEqualTo(1L);
    }
}
