package com.example.jpalab.tasks.t05;

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
 * Тема 05, задание 02. Смена FK: привязка товара к другой категории.
 *
 * Изменение навигационного поля managed-сущности — это «грязь», которую
 * dirty checking переведёт в UPDATE: id товара не меняется, строка не
 * пересоздаётся (нет delete/insert), обновляется только category_id.
 */
@DataJpaTest
class TASK02Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void reassignCategory() {
        Category a = categoryRepository.saveAndFlush(
                Category.builder().name("A").build());
        Category b = categoryRepository.saveAndFlush(
                Category.builder().name("B").build());

        Product product = productRepository.saveAndFlush(Product.builder()
                .name("Switch")
                .price(new BigDecimal("10.00"))
                .stockQuantity(1)
                .category(a)
                .build());
        Long originalId = product.getId();

        // один вызов setCategory + flush
        product.setCategory(b);
        em.flush();

        // id товара не изменился (строка не пересоздавалась)
        assertThat(product.getId()).isEqualTo(originalId);

        // в БД обновился внешний ключ
        Object fk = em.createNativeQuery(
                "SELECT category_id FROM products WHERE id = :id", Object.class)
                .setParameter("id", product.getId())
                .getSingleResult();
        assertThat(fk).isEqualTo(b.getId());

        // в памяти — категория B
        assertThat(product.getCategory().getName()).isEqualTo("B");
    }
}
