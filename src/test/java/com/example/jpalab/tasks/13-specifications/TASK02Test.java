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
 * Тема 13, задание 02. Comparison-предикат: price &gt;= minPrice
 * (граница включается). Метод-артефакт: ProductSpecifications.byPriceAtLeast(BigDecimal).
 */
@DataJpaTest
class TASK02Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void priceAtLeastIsInclusive() {
        Category category = categoryRepository.save(Category.builder().name("Prices").build());
        productRepository.save(Product.builder().name("Cheap").price(new BigDecimal("10.00"))
                .stockQuantity(1).category(category).build());
        productRepository.save(Product.builder().name("Middle").price(new BigDecimal("50.00"))
                .stockQuantity(2).category(category).build());
        productRepository.save(Product.builder().name("Expensive").price(new BigDecimal("100.00"))
                .stockQuantity(3).category(category).build());

        Object specs = TaskTestSupport.newInstance("com.example.jpalab.repository.ProductSpecifications");

        Specification<Product> at50 =
                TaskTestSupport.invoke(specs, "byPriceAtLeast", new Class<?>[]{BigDecimal.class}, new BigDecimal("50.00"));
        List<Product> found = productRepository.findAll(at50);
        // 50.00 входит в результат: сравнение нестрогое
        assertThat(found).extracting(Product::getName).containsExactlyInAnyOrder("Middle", "Expensive");

        Specification<Product> at5001 =
                TaskTestSupport.invoke(specs, "byPriceAtLeast", new Class<?>[]{BigDecimal.class}, new BigDecimal("50.01"));
        assertThat(productRepository.findAll(at5001)).extracting(Product::getName).containsExactly("Expensive");
    }
}
