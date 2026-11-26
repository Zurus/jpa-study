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
 * Тема 14, задание 10. GROUP BY + HAVING через Criteria: multiselect
 * (имя категории, AVG(price)), groupBy, having (AVG >= min, граница
 * включается), orderBy по имени категории. Аналог запроса темы 04,
 * но в программной форме Criteria API (разрешённое исключение).
 * Метод-артефакт: ProductCriteriaService.findCategoryNamesWithAveragePriceAtLeast(BigDecimal).
 *
 * Товар без категории не образует группу. Реализация без HAVING
 * (только group by) вернёт все категории — тест красный.
 */
@DataJpaTest
@Import(ProductCriteriaService.class)
class TASK10Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCriteriaService service;

    @Test
    void groupByWithHavingOnAverage() {
        Category alpha = categoryRepository.save(Category.builder().name("Alpha").build());
        Category beta = categoryRepository.save(Category.builder().name("Beta").build());
        Category gamma = categoryRepository.save(Category.builder().name("Gamma").build());
        // Alpha: avg = 55.00; Beta: avg = 25.00; Gamma: avg = 50.00
        productRepository.save(Product.builder().name("Alpha 1").price(new BigDecimal("10.00"))
                .stockQuantity(1).category(alpha).build());
        productRepository.save(Product.builder().name("Alpha 2").price(new BigDecimal("100.00"))
                .stockQuantity(2).category(alpha).build());
        productRepository.save(Product.builder().name("Beta 1").price(new BigDecimal("20.00"))
                .stockQuantity(3).category(beta).build());
        productRepository.save(Product.builder().name("Beta 2").price(new BigDecimal("30.00"))
                .stockQuantity(4).category(beta).build());
        productRepository.save(Product.builder().name("Gamma 1").price(new BigDecimal("50.00"))
                .stockQuantity(5).category(gamma).build());
        // дорогой товар без категории не попадает ни в одну группу
        productRepository.save(Product.builder().name("Free").price(new BigDecimal("999.00"))
                .stockQuantity(6).category(null).build());

        // avg >= 50.00: Alpha (55), Gamma (50 — граница включается); Beta (25) — нет
        List<String> found = TaskTestSupport.invoke(service, "findCategoryNamesWithAveragePriceAtLeast",
                new Class<?>[]{BigDecimal.class}, new BigDecimal("50.00"));
        assertThat(found).containsExactly("Alpha", "Gamma");

        // avg >= 25.00: все три категории, по алфавиту
        List<String> all = TaskTestSupport.invoke(service, "findCategoryNamesWithAveragePriceAtLeast",
                new Class<?>[]{BigDecimal.class}, new BigDecimal("25.00"));
        assertThat(all).containsExactly("Alpha", "Beta", "Gamma");

        // avg >= 55.00: только Alpha (55 входит), Gamma (50) — нет
        List<String> strict = TaskTestSupport.invoke(service, "findCategoryNamesWithAveragePriceAtLeast",
                new Class<?>[]{BigDecimal.class}, new BigDecimal("55.00"));
        assertThat(strict).containsExactly("Alpha");
    }
}
