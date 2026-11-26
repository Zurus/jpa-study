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
 * Тема 14, задание 06. Сортировка через CriteriaQuery.orderBy: цена по
 * убыванию, при равной цене — имя по возрастанию (тай-брейк).
 * Метод-артефакт: ProductCriteriaService.findSortedPriceDescNameAsc(BigDecimal).
 *
 * Данные: "Beta" (100.00) вставлен ПЕРЕД "Alpha" (100.00) — порядок
 * вставки (id) не совпадает с алфавитным; реализация без тай-брейка
 * по имени даст неверный порядок — тест красный.
 */
@DataJpaTest
@Import(ProductCriteriaService.class)
class TASK06Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCriteriaService service;

    @Test
    void priceDescWithNameTieBreak() {
        Category category = categoryRepository.save(Category.builder().name("Shelf").build());
        // порядок вставки намеренно «против» алфавита для равных цен
        productRepository.save(Product.builder().name("Beta").price(new BigDecimal("100.00"))
                .stockQuantity(1).category(category).build());
        productRepository.save(Product.builder().name("Alpha").price(new BigDecimal("100.00"))
                .stockQuantity(2).category(category).build());
        productRepository.save(Product.builder().name("Gamma").price(new BigDecimal("50.00"))
                .stockQuantity(3).category(category).build());
        productRepository.save(Product.builder().name("Delta").price(new BigDecimal("10.00"))
                .stockQuantity(4).category(category).build());

        List<Product> found = TaskTestSupport.invoke(service, "findSortedPriceDescNameAsc",
                new Class<?>[]{BigDecimal.class}, new BigDecimal("50.00"));
        // цена DESC, при равенстве — имя ASC: Alpha, Beta (оба 100.00), Gamma
        assertThat(found).extracting(Product::getName).containsExactly("Alpha", "Beta", "Gamma");

        List<Product> onlyTop = TaskTestSupport.invoke(service, "findSortedPriceDescNameAsc",
                new Class<?>[]{BigDecimal.class}, new BigDecimal("100.00"));
        assertThat(onlyTop).extracting(Product::getName).containsExactly("Alpha", "Beta");
    }
}
