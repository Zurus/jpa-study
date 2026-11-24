package com.example.jpalab.tasks.t01;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 01, задание 03. Top + OrderBy без предиката.
 *
 * Целевой метод (добавить в ProductRepository, без @Query):
 * <pre>{@code
 * List<Product> findTop5ByOrderByPriceDesc();
 * }</pre>
 * На 7 товарах с ценами 10..70 возвращается ровно 5 самых дорогих,
 * отсортированных по цене по убыванию.
 */
@DataJpaTest
class TASK03Test {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findTop5ByOrderByPriceDescReturnsFiveMostExpensive() {
        // 7 товаров с ценами 10, 20, 30, 40, 50, 60, 70
        for (int price = 10; price <= 70; price += 10) {
            productRepository.save(Product.builder()
                    .name("Product " + price)
                    .price(new BigDecimal(price + ".00"))
                    .stockQuantity(1)
                    .build());
        }

        List<Product> top = invoke(productRepository,
                "findTop5ByOrderByPriceDesc", new Class<?>[]{});

        // ровно 5 из 7
        assertThat(top).hasSize(5);
        // цены по убыванию
        assertThat(top).extracting(Product::getPrice)
                .containsExactly(
                        new BigDecimal("70.00"),
                        new BigDecimal("60.00"),
                        new BigDecimal("50.00"),
                        new BigDecimal("40.00"),
                        new BigDecimal("30.00"));
        // это именно 5 самых дорогих: дешёвые (10, 20) отсутствуют
        assertThat(top).extracting(Product::getName)
                .doesNotContain("Product 10", "Product 20");
    }
}
