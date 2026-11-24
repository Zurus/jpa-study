package com.example.jpalab.tasks.t01;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 01, задание 08. NULL в derived queries.
 *
 * Целевые методы (добавить в ProductRepository, без @Query):
 * <pre>{@code
 * List<Product> findByCategoryIsNull();
 * List<Product> findByCategoryIsNotNull();
 * }</pre>
 * Плюс тест-ловушка: существующий метод findByCategory(Category),
 * вызванный с null, — SQL-сравнение «= null» истинно никогда,
 * поэтому результат пустой список (не ошибка и не товары без категории).
 */
@DataJpaTest
class TASK08Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void nullPredicatesAndNullArgumentTrap() {
        // 3 товара: 2 в категории, 1 без
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

        // товары БЕЗ категории: ровно 1
        List<Product> withoutCategory = invoke(productRepository,
                "findByCategoryIsNull", new Class<?>[]{});
        assertThat(withoutCategory).hasSize(1);
        assertThat(withoutCategory.get(0).getName()).isEqualTo("No Cat C");

        // товары С категорией: ровно 2
        List<Product> withCategory = invoke(productRepository,
                "findByCategoryIsNotNull", new Class<?>[]{});
        assertThat(withCategory).extracting(Product::getName)
                .containsExactlyInAnyOrder("In Cat A", "In Cat B");

        // ловушка: findByCategory(null) — не ошибка, но и не «товары без категории»,
        // а пустой список, потому что сравнение «category = null» всегда ложно
        List<Product> trap = productRepository.findByCategory(null);
        assertThat(trap).isEmpty();
    }
}
