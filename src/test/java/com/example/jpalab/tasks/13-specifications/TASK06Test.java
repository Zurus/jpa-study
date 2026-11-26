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
 * Тема 13, задание 06. Отрицание через not(): товары, у которых имя НЕ
 * содержит фрагмент (case-insensitive). Метод-артефакт:
 * ProductSpecifications.withoutNameContaining(String).
 */
@DataJpaTest
class TASK06Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void notExcludesMatchingNames() {
        Category category = categoryRepository.save(Category.builder().name("Items").build());
        productRepository.save(Product.builder().name("Super Widget").price(new BigDecimal("10.00"))
                .stockQuantity(1).category(category).build());
        productRepository.save(Product.builder().name("super gizmo").price(new BigDecimal("20.00"))
                .stockQuantity(2).category(category).build());
        productRepository.save(Product.builder().name("Boring item").price(new BigDecimal("30.00"))
                .stockQuantity(3).category(category).build());

        Object specs = TaskTestSupport.newInstance("com.example.jpalab.repository.ProductSpecifications");

        // оба «super...» исключаются (регистр не важен)
        Specification<Product> spec =
                TaskTestSupport.invoke(specs, "withoutNameContaining", new Class<?>[]{String.class}, "super");
        List<Product> found = productRepository.findAll(spec);
        assertThat(found).extracting(Product::getName).containsExactly("Boring item");

        // фрагмент не найден ни у кого — negation возвращает все строки
        Specification<Product> none =
                TaskTestSupport.invoke(specs, "withoutNameContaining", new Class<?>[]{String.class}, "zzz");
        assertThat(productRepository.findAll(none)).extracting(Product::getName)
                .containsExactlyInAnyOrder("Super Widget", "super gizmo", "Boring item");
    }
}
