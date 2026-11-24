package com.example.jpalab.repository;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 1. Derived query: имя метода -> запрос.
 * Демонстрируем: фильтрация (WHERE), сортировка (ORDER BY через Sort), LIMIT (Top-3).
 */
@DataJpaTest
class DerivedQueryExampleTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product saveProduct(String name, String price, int stock, Category category) {
        return productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(stock)
                .category(category)
                .build());
    }

    @Test
    void derivedQueryFiltersAndSorts() {
        // Данные, создаваемые самим тестом (каждый тест обкатывается своей транзакцией)
        Category category = categoryRepository.save(Category.builder().name("Gadgets").build());
        saveProduct("Phone A", "500.00", 10, category);
        saveProduct("Smartphone B", "700.00", 5, category);
        saveProduct("Phone D", "300.00", 7, category);
        saveProduct("Tablet C", "900.00", 1, category);
        saveProduct("Camera E", "200.00", 3, category);

        // name LIKE '%pho%' AND price <= 700.00, сортировка передается из кода
        List<Product> found = productRepository.findByNameContainingIgnoreCaseAndPriceLessThanEqual(
                "pho", new BigDecimal("700.00"), Sort.by(Sort.Direction.DESC, "price"));

        // Tablet C и Camera E отфильтрованы; оставшиеся отсортированы по цене по убыванию
        assertThat(found)
                .extracting(Product::getName)
                .containsExactly("Smartphone B", "Phone A", "Phone D");
    }

    @Test
    void derivedQueryAppliesLimitViaTopN() {
        Category category = categoryRepository.save(Category.builder().name("Gadgets").build());
        saveProduct("Phone A", "500.00", 10, category);
        saveProduct("Smartphone B", "700.00", 5, category);
        saveProduct("Phone D", "300.00", 7, category);
        saveProduct("Tablet C", "900.00", 1, category);
        saveProduct("Camera E", "200.00", 3, category);

        // Top-3 из 5 подходящих, упорядоченных по имени
        List<Product> top3 = productRepository.findTop3ByPriceGreaterThanOrderByNameAsc(new BigDecimal("1.00"));

        assertThat(top3)
                .hasSize(3)
                .extracting(Product::getName)
                .containsExactly("Camera E", "Phone A", "Phone D");
    }
}
