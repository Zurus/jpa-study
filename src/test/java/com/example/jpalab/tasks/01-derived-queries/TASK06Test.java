package com.example.jpalab.tasks.t01;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 01, задание 06. Коннектор Or и параметр Sort.
 *
 * Целевой метод (добавить в ProductRepository, без @Query):
 * <pre>{@code
 * List<Product> findByNameContainingOrPriceLessThanEqual(
 *         String nameFragment, BigDecimal maxPrice, Sort sort);
 * }</pre>
 * Имя содержит фрагмент ИЛИ цена не больше заданной; сортировка передаётся
 * из вызывающего кода параметром Sort (не «зашита» в имя метода).
 */
@DataJpaTest
class TASK06Test {

    @Autowired
    private ProductRepository productRepository;

    private void save(String name, String price) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .build());
    }

    private List<Product> call(String fragment, String maxPrice, Sort sort) {
        return invoke(productRepository,
                "findByNameContainingOrPriceLessThanEqual",
                new Class<?>[]{String.class, BigDecimal.class, Sort.class},
                fragment, new BigDecimal(maxPrice), sort);
    }

    @Test
    void orPredicateAndExternalSort() {
        // 4 товара: 2 подходят по имени, 1 по цене, 1 ни по чему
        save("Omega A", "100.00");  // имя содержит "Omega"
        save("Omega B", "110.00");  // имя содержит "Omega"
        save("Small C", "50.00");   // только цена <= 90
        save("Large D", "120.00");  // ни имя, ни цена

        // OR-семантика: объединение двух множеств (3 из 4), а не пересечение
        List<Product> byPriceAsc = call("Omega", "90.00", Sort.by("price").ascending());
        assertThat(byPriceAsc).hasSize(3);
        assertThat(byPriceAsc).extracting(Product::getName)
                .doesNotContain("Large D");
        // сортировка по цене возрастает: Small C (50), Omega A (100), Omega B (110)
        assertThat(byPriceAsc).extracting(Product::getName)
                .containsExactly("Small C", "Omega A", "Omega B");

        // та же выборка, но с другой внешней сортировкой: по имени убывает
        List<Product> byNameDesc = call("Omega", "90.00", Sort.by("name").descending());
        assertThat(byNameDesc).extracting(Product::getName)
                .containsExactly("Omega B", "Omega A", "Small C");
    }
}
