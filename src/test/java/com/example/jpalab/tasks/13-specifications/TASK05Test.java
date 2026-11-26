package com.example.jpalab.tasks.t13;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import org.springframework.data.jpa.domain.Specification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 13, задание 05. Комбинатор or(): имя содержит фрагмент ИЛИ
 * остаток не больше maxStock. Метод-артефакт:
 * ProductSpecifications.nameContainingOrStockAtMost(String, Integer).
 */
@DataJpaTest
class TASK05Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void orMatchesEitherPredicate() {
        Category category = categoryRepository.save(Category.builder().name("Stockroom").build());
        productRepository.save(Product.builder().name("Alpha One").price(new BigDecimal("10.00"))
                .stockQuantity(15).category(category).build());
        productRepository.save(Product.builder().name("Alpha Two").price(new BigDecimal("20.00"))
                .stockQuantity(50).category(category).build());
        productRepository.save(Product.builder().name("Beta").price(new BigDecimal("30.00"))
                .stockQuantity(1).category(category).build());

        Object specs = TaskTestSupport.newInstance("com.example.jpalab.repository.ProductSpecifications");

        // Alpha One — по имени ("ONE"), Beta — по остатку (1 <= 10),
        // Alpha Two не подходит ни по одному из условий
        Specification<Product> spec =
                TaskTestSupport.invoke(specs, "nameContainingOrStockAtMost",
                        new Class<?>[]{String.class, Integer.class}, "ONE", 10);
        List<Product> found = productRepository.findAll(spec);
        assertThat(found).extracting(Product::getName).containsExactlyInAnyOrder("Alpha One", "Beta");

        // имя не совпадает ни с одним — остаётся только ветка по остатку
        Specification<Product> stockOnly =
                TaskTestSupport.invoke(specs, "nameContainingOrStockAtMost",
                        new Class<?>[]{String.class, Integer.class}, "ZZZ", 10);
        assertThat(productRepository.findAll(stockOnly)).extracting(Product::getName).containsExactly("Beta");

        // и имя, и остаток подходят
        Specification<Product> both =
                TaskTestSupport.invoke(specs, "nameContainingOrStockAtMost",
                        new Class<?>[]{String.class, Integer.class}, "BETA", 4);
        assertThat(productRepository.findAll(both)).extracting(Product::getName).containsExactly("Beta");
    }
}
