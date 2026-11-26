package com.example.jpalab.tasks.t14;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.service.ProductCriteriaService;
import com.example.jpalab.tasks.TaskTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 14, задание 02. Диапазон цены через cb.and из двух сравнений
 * (оба конца включаются). Метод-артефакт:
 * ProductCriteriaService.findPriceBetween(BigDecimal, BigDecimal).
 */
@DataJpaTest
@Import(ProductCriteriaService.class)
class TASK02Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCriteriaService service;

    @Test
    void priceBetweenIsInclusive() {
        Category category = categoryRepository.save(Category.builder().name("Prices").build());
        productRepository.save(Product.builder().name("P10").price(new BigDecimal("10.00"))
                .stockQuantity(1).category(category).build());
        productRepository.save(Product.builder().name("P50").price(new BigDecimal("50.00"))
                .stockQuantity(2).category(category).build());
        productRepository.save(Product.builder().name("P100").price(new BigDecimal("100.00"))
                .stockQuantity(3).category(category).build());

        List<Product> middle = TaskTestSupport.invoke(service, "findPriceBetween",
                new Class<?>[]{BigDecimal.class, BigDecimal.class},
                new BigDecimal("50.00"), new BigDecimal("100.00"));
        assertThat(middle).extracting(Product::getName).containsExactlyInAnyOrder("P50", "P100");

        List<Product> low = TaskTestSupport.invoke(service, "findPriceBetween",
                new Class<?>[]{BigDecimal.class, BigDecimal.class},
                new BigDecimal("0.00"), new BigDecimal("10.00"));
        assertThat(low).extracting(Product::getName).containsExactly("P10");

        // промежуток между ценами — пусто
        List<Product> gap = TaskTestSupport.invoke(service, "findPriceBetween",
                new Class<?>[]{BigDecimal.class, BigDecimal.class},
                new BigDecimal("10.01"), new BigDecimal("49.99"));
        assertThat(gap).isEmpty();
    }
}
