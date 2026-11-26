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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 14, задание 08. Агрегатная функция: SUM(price) по Join-категории;
 * пустая группа — null (стандартная SQL-семантика агрегата).
 * Метод-артефакт: ProductCriteriaService.sumPricesInCategory(String).
 */
@DataJpaTest
@Import(ProductCriteriaService.class)
class TASK08Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCriteriaService service;

    @Test
    void sumPricesInCategory() {
        Category alpha = categoryRepository.save(Category.builder().name("Alpha").build());
        Category beta = categoryRepository.save(Category.builder().name("Beta").build());
        productRepository.save(Product.builder().name("Alpha 1").price(new BigDecimal("10.50"))
                .stockQuantity(1).category(alpha).build());
        productRepository.save(Product.builder().name("Alpha 2").price(new BigDecimal("20.25"))
                .stockQuantity(2).category(alpha).build());
        productRepository.save(Product.builder().name("Beta 1").price(new BigDecimal("999.99"))
                .stockQuantity(3).category(beta).build());
        // товар без категории в сумму любой категории не входит
        productRepository.save(Product.builder().name("Free").price(new BigDecimal("77.77"))
                .stockQuantity(4).category(null).build());

        BigDecimal alphaSum =
                TaskTestSupport.invoke(service, "sumPricesInCategory", new Class<?>[]{String.class}, "Alpha");
        assertThat(alphaSum).isNotNull().isEqualByComparingTo("30.75");

        BigDecimal betaSum =
                TaskTestSupport.invoke(service, "sumPricesInCategory", new Class<?>[]{String.class}, "Beta");
        assertThat(betaSum).isEqualByComparingTo("999.99");

        // категории без товаров — SUM по пустому набору = null
        BigDecimal none =
                TaskTestSupport.invoke(service, "sumPricesInCategory", new Class<?>[]{String.class}, "Gamma");
        assertThat(none).isNull();
    }
}
