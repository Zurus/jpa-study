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
 * Тема 14, задание 09. Подзапрос: товары из категорий, в которых есть
 * хотя бы один товар с ценой &gt;= threshold. Метод-артефакт:
 * ProductCriteriaService.findInCategoriesWithPriceAtLeast(BigDecimal).
 *
 * Данные построены так, что оба наивных варианта дают красный тест:
 * «цена &gt;= threshold» добавит дорогой товар БЕЗ категории,
 * «все товары, у которых есть категория» добавит и дешёвые из Beta.
 */
@DataJpaTest
@Import(ProductCriteriaService.class)
class TASK09Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCriteriaService service;

    @Test
    void subqueryOnExpensiveCategories() {
        Category alpha = categoryRepository.save(Category.builder().name("Alpha").build());
        Category beta = categoryRepository.save(Category.builder().name("Beta").build());
        productRepository.save(Product.builder().name("Alpha 1").price(new BigDecimal("10.00"))
                .stockQuantity(1).category(alpha).build());
        productRepository.save(Product.builder().name("Alpha 2").price(new BigDecimal("200.00"))
                .stockQuantity(2).category(alpha).build());
        productRepository.save(Product.builder().name("Beta 1").price(new BigDecimal("50.00"))
                .stockQuantity(3).category(beta).build());
        productRepository.save(Product.builder().name("Beta 2").price(new BigDecimal("60.00"))
                .stockQuantity(4).category(beta).build());
        // дорогой товар БЕЗ категории: не «в категории с дорогим товаром»
        productRepository.save(Product.builder().name("Free").price(new BigDecimal("150.00"))
                .stockQuantity(5).category(null).build());

        // дороговата только Alpha (200.00 >= 100.00) → оба товара Alpha,
        // включая дешёвый "Alpha 1"; "Free" и Beta — исключены
        List<Product> found = TaskTestSupport.invoke(service, "findInCategoriesWithPriceAtLeast",
                new Class<?>[]{BigDecimal.class}, new BigDecimal("100.00"));
        assertThat(found).extracting(Product::getName).containsExactlyInAnyOrder("Alpha 1", "Alpha 2");

        // теперь и Beta дороговата (50.00/60.00 >= 50.00) → все 4 с категорией
        List<Product> broader = TaskTestSupport.invoke(service, "findInCategoriesWithPriceAtLeast",
                new Class<?>[]{BigDecimal.class}, new BigDecimal("50.00"));
        assertThat(broader).extracting(Product::getName)
                .containsExactlyInAnyOrder("Alpha 1", "Alpha 2", "Beta 1", "Beta 2");
    }
}
