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
 * Тема 05, задание 09. Merge отстёгнутого товара со сменой категории.
 *
 * merge — это «применить diff отстёгнутого к управляемому»: навигационные
 * поля копируются вместе со скалярами; «отстёгнутый» объект по-прежнему
 * detached, результат merge — managed-копия.
 */
@DataJpaTest
class TASK09Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void mergeDetachedWithNewCategory() {
        Category a = categoryRepository.saveAndFlush(
                Category.builder().name("A").build());
        Category b = categoryRepository.saveAndFlush(
                Category.builder().name("B").build());

        Product product = productRepository.saveAndFlush(Product.builder()
                .name("Merged")
                .price(new BigDecimal("10.00"))
                .stockQuantity(1)
                .category(a)
                .build());
        Long versionBefore = product.getVersion();

        // отстёгивание
        em.clear();
        Product detached = productRepository.findById(product.getId()).orElseThrow();
        em.clear();

        // изменение у detached-копии
        detached.setCategory(b);

        // merge (JpaRepository.save -> merge для существующей сущности)
        Product managed = productRepository.save(detached);
        em.flush();

        // результат merge — ДРУГАЯ инстанция, чем переданная
        assertThat(managed).isNotSameAs(detached);
        // версия увеличилась на 1
        assertThat(managed.getVersion()).isEqualTo(versionBefore + 1);

        // в БД: category_id = B, и одна строка товара (не две)
        Object[] row = (Object[]) em.createNativeQuery("""
                SELECT category_id, COUNT(*) FROM products WHERE id = :id
                """, Object[].class)
                .setParameter("id", product.getId())
                .getSingleResult();
        assertThat(row[0]).isEqualTo(b.getId());
        assertThat(row[1]).isEqualTo(1L);
    }
}
