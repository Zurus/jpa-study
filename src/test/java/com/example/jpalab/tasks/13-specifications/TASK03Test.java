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
 * Тема 13, задание 03. Динамический фильтр: фрагмент null — предикат
 * «всегда верен» (все строки), иначе — case-insensitive contains по name.
 * Метод-артефакт: ProductSpecifications.byNameContainingIgnoreCaseOptional(String).
 */
@DataJpaTest
class TASK03Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void nullFragmentMatchesEverything() {
        Category category = categoryRepository.save(Category.builder().name("Items").build());
        productRepository.save(Product.builder().name("Super Widget").price(new BigDecimal("10.00"))
                .stockQuantity(1).category(category).build());
        productRepository.save(Product.builder().name("super gizmo").price(new BigDecimal("20.00"))
                .stockQuantity(2).category(category).build());
        productRepository.save(Product.builder().name("Boring item").price(new BigDecimal("30.00"))
                .stockQuantity(3).category(category).build());

        Object specs = TaskTestSupport.newInstance("com.example.jpalab.repository.ProductSpecifications");

        // null — фильтр «выключен»: возвращаются все строки
        Specification<Product> off =
                TaskTestSupport.invoke(specs, "byNameContainingIgnoreCaseOptional", new Class<?>[]{String.class}, new Object[]{null});
        assertThat(productRepository.findAll(off)).extracting(Product::getName)
                .containsExactlyInAnyOrder("Super Widget", "super gizmo", "Boring item");

        // регистр не важен
        Specification<Product> widg =
                TaskTestSupport.invoke(specs, "byNameContainingIgnoreCaseOptional", new Class<?>[]{String.class}, "WIDG");
        assertThat(productRepository.findAll(widg)).extracting(Product::getName).containsExactly("Super Widget");

        Specification<Product> superWord =
                TaskTestSupport.invoke(specs, "byNameContainingIgnoreCaseOptional", new Class<?>[]{String.class}, "SUPER");
        assertThat(productRepository.findAll(superWord)).extracting(Product::getName)
                .containsExactlyInAnyOrder("Super Widget", "super gizmo");
    }
}
