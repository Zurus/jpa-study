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
 * Тема 13, задание 10. Полная динамическая спецификация: диапазон цены
 * [from, to] (концы включаются, любой может быть null — сторона «открыта»),
 * точная категория (null — без фильтра) и минимальный остаток (null — без
 * фильтра). Метод-артефакт: ProductSpecifications.search(BigDecimal, BigDecimal, String, Integer).
 *
 * Один из товаров БЕЗ категории: если реализация всегда строит join к
 * category, он теряется — тест красный.
 */
@DataJpaTest
class TASK10Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Specification<Product> search(Object specs,
                                          BigDecimal priceFrom, BigDecimal priceTo,
                                          String categoryName, Integer minStock) {
        return TaskTestSupport.invoke(specs, "search",
                new Class<?>[]{BigDecimal.class, BigDecimal.class, String.class, Integer.class},
                priceFrom, priceTo, categoryName, minStock);
    }

    @Test
    void openEndedRangeAndCategory() {
        Category alpha = categoryRepository.save(Category.builder().name("Alpha").build());
        Category beta = categoryRepository.save(Category.builder().name("Beta").build());
        productRepository.save(Product.builder().name("Alpha 1").price(new BigDecimal("10.00"))
                .stockQuantity(0).category(alpha).build());
        productRepository.save(Product.builder().name("Alpha 2").price(new BigDecimal("50.00"))
                .stockQuantity(10).category(alpha).build());
        productRepository.save(Product.builder().name("Beta 1").price(new BigDecimal("50.00"))
                .stockQuantity(20).category(beta).build());
        // товар без категории: join к category не должен строиться, если фильтр по категории выключен
        productRepository.save(Product.builder().name("Free").price(new BigDecimal("100.00"))
                .stockQuantity(5).category(null).build());

        Object specs = TaskTestSupport.newInstance("com.example.jpalab.repository.ProductSpecifications");

        // все аргументы null — все 4 строки (включая товар без категории)
        List<Product> all = productRepository.findAll(search(specs, null, null, null, null));
        assertThat(all).extracting(Product::getName)
                .containsExactlyInAnyOrder("Alpha 1", "Alpha 2", "Beta 1", "Free");

        // только нижняя граница (включается): 50.00 входит
        assertThat(productRepository.findAll(search(specs, new BigDecimal("50.00"), null, null, null)))
                .extracting(Product::getName).containsExactlyInAnyOrder("Alpha 2", "Beta 1", "Free");

        // только верхняя граница (включается)
        assertThat(productRepository.findAll(search(specs, null, new BigDecimal("50.00"), null, null)))
                .extracting(Product::getName).containsExactlyInAnyOrder("Alpha 1", "Alpha 2", "Beta 1");

        // точечный диапазон + категория + остаток: только Beta 1 (Alpha 2 — не та категория)
        assertThat(productRepository.findAll(
                search(specs, new BigDecimal("50.00"), new BigDecimal("50.00"), "Beta", 10)))
                .extracting(Product::getName).containsExactly("Beta 1");

        // фильтр по категории: товар без категории исключается, когда фильтр ВКЛЮЧЁН
        assertThat(productRepository.findAll(search(specs, null, null, "Alpha", null)))
                .extracting(Product::getName).containsExactlyInAnyOrder("Alpha 1", "Alpha 2");

        // только минимальный остаток: Free (5) и Alpha 1 (0) исключены
        assertThat(productRepository.findAll(search(specs, null, null, null, 10)))
                .extracting(Product::getName).containsExactlyInAnyOrder("Alpha 2", "Beta 1");
    }
}
