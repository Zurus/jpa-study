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
 * Тема 14, задание 03. Выборка Path вместо целой сущности:
 * CriteriaQuery&lt;String&gt; возвращает имена, а не Product.
 * Метод-артефакт: ProductCriteriaService.findNamesByStockAtLeast(int).
 */
@DataJpaTest
@Import(ProductCriteriaService.class)
class TASK03Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCriteriaService service;

    @Test
    void selectsPathNotEntity() {
        Category category = categoryRepository.save(Category.builder().name("Stockroom").build());
        productRepository.save(Product.builder().name("Empty").price(new BigDecimal("10.00"))
                .stockQuantity(0).category(category).build());
        productRepository.save(Product.builder().name("Few").price(new BigDecimal("20.00"))
                .stockQuantity(5).category(category).build());
        productRepository.save(Product.builder().name("Many").price(new BigDecimal("30.00"))
                .stockQuantity(50).category(category).build());

        List<String> names =
                TaskTestSupport.invoke(service, "findNamesByStockAtLeast", new Class<?>[]{int.class}, 5);

        // результат — список строк (Path), а не сущностей; сравнение нестрогое (5 входит)
        assertThat(names)
                .allSatisfy(n -> assertThat(n).isInstanceOf(String.class))
                .containsExactlyInAnyOrder("Few", "Many");

        List<String> all =
                TaskTestSupport.invoke(service, "findNamesByStockAtLeast", new Class<?>[]{int.class}, 0);
        assertThat(all).containsExactlyInAnyOrder("Empty", "Few", "Many");
    }
}
