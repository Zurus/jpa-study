package com.example.jpalab.tasks.t01;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 01, задание 04. Комбинация условий: Containing + Between (AND).
 *
 * Целевой метод (добавить в ProductRepository, без @Query):
 * <pre>{@code
 * List<Product> findByNameContainingIgnoreCaseAndPriceBetween(
 *         String nameFragment, BigDecimal minPrice, BigDecimal maxPrice);
 * }</pre>
 * Имя содержит фрагмент (без учёта регистра) И цена в диапазоне [от, до]
 * включительно. Регистронезависимость зафиксирована в имени метода (IgnoreCase).
 */
@DataJpaTest
class TASK04Test {

    @Autowired
    private ProductRepository productRepository;

    private void save(String name, String price) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .build());
    }

    @Test
    void nameContainingAndPriceBetweenIncludesBoundaries() {
        // данные: "Alpha 100", "Alpha 200", "Alpha 300", "Beta 200"
        save("Alpha 100", "100.00");
        save("Alpha 200", "200.00");
        save("Alpha 300", "300.00");
        save("Beta 200", "200.00");

        // фрагмент "alpha" (нижний регистр), диапазон [100, 200]
        List<Product> found = invoke(productRepository,
                "findByNameContainingIgnoreCaseAndPriceBetween",
                new Class<?>[]{String.class, BigDecimal.class, BigDecimal.class},
                "alpha", new BigDecimal("100.00"), new BigDecimal("200.00"));

        // границы диапазона включены: товары с ценой ровно 100 и ровно 200 есть
        assertThat(found).extracting(Product::getName)
                .containsExactlyInAnyOrder("Alpha 100", "Alpha 200");
        // "Alpha 300" отфильтрован по цене (300 > 200)
        // "Beta 200" отфильтрован по имени (не содержит "alpha")
        assertThat(found).extracting(Product::getName)
                .doesNotContain("Alpha 300", "Beta 200");
    }
}
