package com.example.jpalab.repository;

import com.example.jpalab.dto.ProductNamePrice;
import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 2. JPQL @Query: именованные параметры, LIKE, сортировка, выборка отдельных полей.
 */
@DataJpaTest
class JpqlQueryExampleTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private void saveProduct(String name, String price, int stock, Category category) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(stock)
                .category(category)
                .build());
    }

    @Test
    void jpqlWithNamedParamLikeAndSorting() {
        Category category = categoryRepository.save(Category.builder().name("Gadgets").build());
        saveProduct("Phone A", "500.00", 10, category);
        saveProduct("Smartphone B", "700.00", 5, category);
        saveProduct("Phone D", "300.00", 7, category);
        saveProduct("Tablet C", "900.00", 1, category);
        saveProduct("Camera E", "200.00", 3, category);

        // LIKE с именованным параметром :fragment; ORDER BY price DESC задан в JPQL
        List<Product> found = productRepository.searchByNameFragment("pho");

        assertThat(found)
                .extracting(Product::getName)
                .containsExactly("Smartphone B", "Phone A", "Phone D");
    }

    @Test
    void jpqlProjectionReturnsOnlyRequestedFields() {
        Category category = categoryRepository.save(Category.builder().name("Gadgets").build());
        saveProduct("Phone A", "500.00", 10, category);
        saveProduct("Smartphone B", "700.00", 5, category);
        saveProduct("Phone D", "300.00", 7, category);

        // Проекция: в память попадают только (name, price), а не целые сущности
        List<ProductNamePrice> pairs = productRepository.namePricesAtLeast(new BigDecimal("500.00"));

        assertThat(pairs).containsExactly(
                new ProductNamePrice("Phone A", new BigDecimal("500.00")),
                new ProductNamePrice("Smartphone B", new BigDecimal("700.00")));
    }
}
