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
 * Тема 13, задание 09. Ловушка приоритета and/or: ищем товары, чьё имя
 * содержит фрагмент И (price &gt;= minPrice ИЛИ stock &lt;= maxStock) —
 * OR сгруппирован внутри условия по имени. Метод-артефакт:
 * ProductSpecifications.namedAndPricedOrScarce(String, BigDecimal, Integer).
 *
 * Данные построены так, что неверная группировка
 * (name AND price) OR stock даст лишний товар "Beta Two"
 * (имя не совпадает, но остаток мал) — тест красный.
 */
@DataJpaTest
class TASK09Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void precedenceRequiresExplicitGrouping() {
        Category category = categoryRepository.save(Category.builder().name("Pool").build());
        productRepository.save(Product.builder().name("Alpha One").price(new BigDecimal("100.00"))
                .stockQuantity(1).category(category).build());
        productRepository.save(Product.builder().name("Alpha Two").price(new BigDecimal("10.00"))
                .stockQuantity(1).category(category).build());
        productRepository.save(Product.builder().name("Beta").price(new BigDecimal("100.00"))
                .stockQuantity(50).category(category).build());
        productRepository.save(Product.builder().name("Beta Two").price(new BigDecimal("10.00"))
                .stockQuantity(1).category(category).build());

        Object specs = TaskTestSupport.newInstance("com.example.jpalab.repository.ProductSpecifications");

        Specification<Product> spec =
                TaskTestSupport.invoke(specs, "namedAndPricedOrScarce",
                        new Class<?>[]{String.class, BigDecimal.class, Integer.class},
                        "ALPHA", new BigDecimal("100.00"), 10);
        List<Product> found = productRepository.findAll(spec);

        // Alpha One: имя + цена; Alpha Two: имя + малый остаток.
        // Beta и Beta Two исключены: имя не содержит "ALPHA" —
        // ветка (price OR stock) без совпадения по имени не считается.
        assertThat(found).extracting(Product::getName)
                .containsExactlyInAnyOrder("Alpha One", "Alpha Two");

        // та же формула для другой категории имён
        Specification<Product> betaSpec =
                TaskTestSupport.invoke(specs, "namedAndPricedOrScarce",
                        new Class<?>[]{String.class, BigDecimal.class, Integer.class},
                        "BETA", new BigDecimal("100.00"), 10);
        List<Product> betaFound = productRepository.findAll(betaSpec);
        assertThat(betaFound).extracting(Product::getName)
                .containsExactlyInAnyOrder("Beta", "Beta Two");
    }
}
