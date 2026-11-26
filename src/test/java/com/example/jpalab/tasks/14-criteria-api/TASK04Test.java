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
 * Тема 14, задание 04. Join по many-to-one: предикат по пути join-категории.
 * Товар без категории (inner join) в результате не появляется.
 * Метод-артефакт: ProductCriteriaService.findByCategoryName(String).
 */
@DataJpaTest
@Import(ProductCriteriaService.class)
class TASK04Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCriteriaService service;

    @Test
    void innerJoinByCategoryName() {
        Category alpha = categoryRepository.save(Category.builder().name("Alpha").build());
        Category beta = categoryRepository.save(Category.builder().name("Beta").build());
        productRepository.save(Product.builder().name("Alpha 1").price(new BigDecimal("10.00"))
                .stockQuantity(1).category(alpha).build());
        productRepository.save(Product.builder().name("Alpha 2").price(new BigDecimal("20.00"))
                .stockQuantity(2).category(alpha).build());
        productRepository.save(Product.builder().name("Beta 1").price(new BigDecimal("30.00"))
                .stockQuantity(3).category(beta).build());
        // товар без категории: inner join его исключает из любого результата
        productRepository.save(Product.builder().name("Free").price(new BigDecimal("40.00"))
                .stockQuantity(4).category(null).build());

        List<Product> alphaFound =
                TaskTestSupport.invoke(service, "findByCategoryName", new Class<?>[]{String.class}, "Alpha");
        assertThat(alphaFound).extracting(Product::getName)
                .containsExactlyInAnyOrder("Alpha 1", "Alpha 2");

        List<Product> betaFound =
                TaskTestSupport.invoke(service, "findByCategoryName", new Class<?>[]{String.class}, "Beta");
        assertThat(betaFound).extracting(Product::getName).containsExactly("Beta 1");

        List<Product> none =
                TaskTestSupport.invoke(service, "findByCategoryName", new Class<?>[]{String.class}, "Gamma");
        assertThat(none).isEmpty();

        // "Free" не должен попасть ни в один из результатов выше
        assertThat(alphaFound).allSatisfy(p -> assertThat(p.getCategory()).isNotNull());
    }
}
