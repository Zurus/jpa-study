package com.example.jpalab.tasks.t03;

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
import java.util.List;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 03, задание 08. NULL в native SQL.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query(value = "SELECT * FROM products WHERE category_id IS NULL", nativeQuery = true)
 * List<Product> findNativeWithoutCategory();
 * }</pre>
 * SQL-семантика: сравнение с NULL — отдельная операция IS NULL;
 * «NULL = NULL» не является TRUE, поэтому «category_id = :id» с null
 * молча не находит ничего (пустой список, а не товары без категории).
 */
@DataJpaTest
class TASK08Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void nullSemantics() {
        // 3 товара: 2 с категорией, 1 без
        Category category = categoryRepository.saveAndFlush(
                Category.builder().name("Cat").build());
        productRepository.save(Product.builder()
                .name("In Cat A").price(new BigDecimal("10.00")).stockQuantity(1)
                .category(category).build());
        productRepository.save(Product.builder()
                .name("In Cat B").price(new BigDecimal("20.00")).stockQuantity(1)
                .category(category).build());
        productRepository.save(Product.builder()
                .name("No Cat C").price(new BigDecimal("30.00")).stockQuantity(1)
                .build());

        // метод с IS NULL — ровно 1 товар
        List<Product> withoutCategory = invoke(productRepository,
                "findNativeWithoutCategory", new Class<?>[]{});
        assertThat(withoutCategory).hasSize(1);
        assertThat(withoutCategory.get(0).getName()).isEqualTo("No Cat C");

        // условие «= :id» с null-параметром — ПУСТОЙ список (0 строк)
        List<Product> nullEquals = em.createNativeQuery(
                "SELECT * FROM products WHERE category_id = :id", Product.class)
                .setParameter("id", null)
                .getResultList();
        assertThat(nullEquals).isEmpty();
    }
}
