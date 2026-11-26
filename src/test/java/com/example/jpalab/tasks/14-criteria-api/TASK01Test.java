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
 * Тема 14, задание 01. Первый программный запрос: CriteriaQuery&lt;Product&gt;
 * с предикатом eq по name. Метод-артефакт:
 * ProductCriteriaService.findByName(String).
 */
@DataJpaTest
@Import(ProductCriteriaService.class)
class TASK01Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCriteriaService service;

    @Test
    void criteriaFindByNameIsExactMatch() {
        Category category = categoryRepository.save(Category.builder().name("Misc").build());
        productRepository.save(Product.builder().name("Widget A").price(new BigDecimal("10.00"))
                .stockQuantity(1).category(category).build());
        productRepository.save(Product.builder().name("Widget B").price(new BigDecimal("20.00"))
                .stockQuantity(2).category(category).build());
        productRepository.save(Product.builder().name("Gadget").price(new BigDecimal("30.00"))
                .stockQuantity(0).category(category).build());

        List<Product> found =
                TaskTestSupport.invoke(service, "findByName", new Class<?>[]{String.class}, "Widget B");
        assertThat(found).extracting(Product::getName).containsExactly("Widget B");

        // точное совпадение: "Widget" сам по себе не находит ничего
        List<Product> partial =
                TaskTestSupport.invoke(service, "findByName", new Class<?>[]{String.class}, "Widget");
        assertThat(partial).isEmpty();
    }
}
