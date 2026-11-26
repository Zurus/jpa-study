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
 * Тема 14, задание 05. LEFT JOIN + динамический WHERE: null-имя категории —
 * фильтр выключен, возвращаются все товары ВКЛЮЧАЯ товар без категории.
 * Метод-артефакт: ProductCriteriaService.findByCategoryNameLeftJoin(String).
 */
@DataJpaTest
@Import(ProductCriteriaService.class)
class TASK05Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCriteriaService service;

    @Test
    void leftJoinKeepsOrphanWhenFilterOff() {
        Category alpha = categoryRepository.save(Category.builder().name("Alpha").build());
        Category beta = categoryRepository.save(Category.builder().name("Beta").build());
        productRepository.save(Product.builder().name("Alpha 1").price(new BigDecimal("10.00"))
                .stockQuantity(1).category(alpha).build());
        productRepository.save(Product.builder().name("Alpha 2").price(new BigDecimal("20.00"))
                .stockQuantity(2).category(alpha).build());
        productRepository.save(Product.builder().name("Beta 1").price(new BigDecimal("30.00"))
                .stockQuantity(3).category(beta).build());
        productRepository.save(Product.builder().name("Free").price(new BigDecimal("40.00"))
                .stockQuantity(4).category(null).build());

        // null — фильтр по категории выключен: все 4 товара, включая "Free"
        List<Product> all =
                TaskTestSupport.invoke(service, "findByCategoryNameLeftJoin", new Class<?>[]{String.class}, new Object[]{null});
        assertThat(all).extracting(Product::getName)
                .containsExactlyInAnyOrder("Alpha 1", "Alpha 2", "Beta 1", "Free");
        assertThat(all).anySatisfy(p -> assertThat(p.getCategory()).isNull());

        // включённый фильтр: только товары категории
        List<Product> alphaFound =
                TaskTestSupport.invoke(service, "findByCategoryNameLeftJoin", new Class<?>[]{String.class}, "Alpha");
        assertThat(alphaFound).extracting(Product::getName)
                .containsExactlyInAnyOrder("Alpha 1", "Alpha 2");
    }
}
