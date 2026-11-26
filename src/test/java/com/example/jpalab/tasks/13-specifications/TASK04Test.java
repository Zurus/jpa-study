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
 * Тема 13, задание 04. Композиция переиспользуемых спецификаций через and():
 * категория + минимальная цена. Метод-артефакт:
 * ProductSpecifications.inCategoryWithPriceAtLeast(String, BigDecimal).
 */
@DataJpaTest
class TASK04Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void andCombinesCategoryAndPrice() {
        Category alpha = categoryRepository.save(Category.builder().name("Alpha").build());
        Category beta = categoryRepository.save(Category.builder().name("Beta").build());
        productRepository.save(Product.builder().name("Alpha 1").price(new BigDecimal("10.00"))
                .stockQuantity(1).category(alpha).build());
        productRepository.save(Product.builder().name("Alpha 2").price(new BigDecimal("100.00"))
                .stockQuantity(2).category(alpha).build());
        productRepository.save(Product.builder().name("Beta 1").price(new BigDecimal("200.00"))
                .stockQuantity(3).category(beta).build());

        Object specs = TaskTestSupport.newInstance("com.example.jpalab.repository.ProductSpecifications");

        Specification<Product> spec =
                TaskTestSupport.invoke(specs, "inCategoryWithPriceAtLeast",
                        new Class<?>[]{String.class, BigDecimal.class}, "Alpha", new BigDecimal("50.00"));
        List<Product> found = productRepository.findAll(spec);
        // и категория, и цена должны совпадать одновременно
        assertThat(found).extracting(Product::getName).containsExactly("Alpha 2");

        Specification<Product> lowBar =
                TaskTestSupport.invoke(specs, "inCategoryWithPriceAtLeast",
                        new Class<?>[]{String.class, BigDecimal.class}, "Alpha", new BigDecimal("5.00"));
        assertThat(productRepository.findAll(lowBar)).extracting(Product::getName)
                .containsExactlyInAnyOrder("Alpha 1", "Alpha 2");

        Specification<Product> betaSpec =
                TaskTestSupport.invoke(specs, "inCategoryWithPriceAtLeast",
                        new Class<?>[]{String.class, BigDecimal.class}, "Beta", new BigDecimal("50.00"));
        assertThat(productRepository.findAll(betaSpec)).extracting(Product::getName).containsExactly("Beta 1");
    }
}
