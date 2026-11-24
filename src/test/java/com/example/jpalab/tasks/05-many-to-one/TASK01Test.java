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
 * Тема 05, задание 01. Первый ManyToOne: FK null и FK установлен.
 *
 * ManyToOne — «владелец» связи: колонку category_id он заполняет сам
 * при сохранении/обновлении. Реальное значение колонки проверяется
 * через native-запрос.
 */
@DataJpaTest
class TASK01Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    private Object categoryColumn(Long productId) {
        return em.createNativeQuery(
                "SELECT category_id FROM products WHERE id = :id", Object.class)
                .setParameter("id", productId)
                .getSingleResult();
    }

    @Test
    void fkLifecycle() {
        // 1) товар БЕЗ категории: category_id в БД — NULL
        Product product = productRepository.saveAndFlush(Product.builder()
                .name("NoCat")
                .price(new BigDecimal("10.00"))
                .stockQuantity(1)
                .build());
        assertThat(categoryColumn(product.getId())).isNull();

        // 2) привязка к категории: тот же товар (save -> managed update)
        Category category = categoryRepository.saveAndFlush(
                Category.builder().name("Cat").build());
        product.setCategory(category);
        productRepository.save(product);
        em.flush();

        // 3) category_id установлен на id категории
        assertThat(categoryColumn(product.getId())).isEqualTo(category.getId());
    }
}
