package com.example.jpalab.tasks.t06;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 06, задание 06. Stale-коллекция: вставка «со стороны товара».
 *
 * Данные LAZY-коллекции — это снапшот момента инициализации: внешнее
 * (из-за спины) изменение не «пробирается» в уже загруженную коллекцию
 * до refresh/новой транзакции.
 */
@DataJpaTest
class TASK06Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void staleCollectionAfterSideInsert() {
        Category category = Category.builder().name("StaleCat").build();
        Product first = Product.builder()
                .name("First").price(new BigDecimal("1.00")).stockQuantity(1).category(category).build();
        category.getProducts().add(first);
        first.setCategory(category);
        categoryRepository.saveAndFlush(category);
        em.clear();

        // коллекция инициализирована: size = 1
        Category managed = categoryRepository.findById(category.getId()).orElseThrow();
        assertThat(managed.getProducts()).hasSize(1);

        // НЕ трогая коллекцию, со стороны товара: новый товар с category = A
        productRepository.save(Product.builder()
                .name("Second")
                .price(new BigDecimal("2.00"))
                .stockQuantity(1)
                .category(managed)
                .build());
        em.flush();

        // уже загруженная коллекция «не узнала» о новом товаре
        assertThat(managed.getProducts()).hasSize(1);
        // в БД у A уже 2 товара
        Long inDb = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM products WHERE category_id = ?", Long.class)
                .setParameter(1, category.getId())
                .getSingleResult();
        assertThat(inDb).isEqualTo(2L);

        // em.refresh лечит: коллекция перечитана
        em.refresh(managed);
        assertThat(managed.getProducts()).hasSize(2);
    }
}
