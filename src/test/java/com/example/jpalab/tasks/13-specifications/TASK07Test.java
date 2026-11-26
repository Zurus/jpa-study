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
 * Тема 13, задание 07. Динамический поиск: три независимых предиката
 * (name contains, price &gt;=, stock &lt;=) объединяются and(); каждый
 * аргумент null — его предикат «всегда верен». Метод-артефакт:
 * ProductSpecifications.searchByNameAndPriceAndStock(String, BigDecimal, Integer).
 */
@DataJpaTest
class TASK07Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Specification<Product> search(Object specs, String name, BigDecimal minPrice, Integer maxStock) {
        return TaskTestSupport.invoke(specs, "searchByNameAndPriceAndStock",
                new Class<?>[]{String.class, BigDecimal.class, Integer.class}, name, minPrice, maxStock);
    }

    @Test
    void allFiltersAreOptional() {
        Category category = categoryRepository.save(Category.builder().name("Warehouse").build());
        productRepository.save(Product.builder().name("Widget One").price(new BigDecimal("10.00"))
                .stockQuantity(1).category(category).build());
        productRepository.save(Product.builder().name("Widget Two").price(new BigDecimal("100.00"))
                .stockQuantity(50).category(category).build());
        productRepository.save(Product.builder().name("Gadget").price(new BigDecimal("50.00"))
                .stockQuantity(5).category(category).build());

        Object specs = TaskTestSupport.newInstance("com.example.jpalab.repository.ProductSpecifications");

        // все аргументы null — все строки
        List<Product> all = productRepository.findAll(search(specs, null, null, null));
        assertThat(all).extracting(Product::getName)
                .containsExactlyInAnyOrder("Widget One", "Widget Two", "Gadget");

        // только имя
        assertThat(productRepository.findAll(search(specs, "WID", null, null)))
                .extracting(Product::getName).containsExactlyInAnyOrder("Widget One", "Widget Two");

        // только цена (граница включается)
        assertThat(productRepository.findAll(search(specs, null, new BigDecimal("50.00"), null)))
                .extracting(Product::getName).containsExactlyInAnyOrder("Widget Two", "Gadget");

        // только остаток
        assertThat(productRepository.findAll(search(specs, null, null, 10)))
                .extracting(Product::getName).containsExactlyInAnyOrder("Widget One", "Gadget");

        // все три вместе
        assertThat(productRepository.findAll(
                search(specs, "WID", new BigDecimal("50.00"), 50)))
                .extracting(Product::getName).containsExactly("Widget Two");
    }
}
