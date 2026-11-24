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
 * Тема 01, задание 01. Простой предикат GreaterThan.
 *
 * Целевой метод (добавить в ProductRepository, без @Query):
 * <pre>{@code
 * List<Product> findByStockQuantityGreaterThan(int minStock);
 * }</pre>
 * Тест создаёт 3 товара с разным stockQuantity и проверяет СТРОГОЕ «больше».
 */
@DataJpaTest
class TASK01Test {

    @Autowired
    private ProductRepository productRepository;

    private Product save(String name, String price, int stockQuantity) {
        return productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(stockQuantity)
                .build());
    }

    @Test
    void derivedGreaterThanFiltersByStockQuantity() {
        // данные: stock 0, 5, 10
        save("Zero", "10.00", 0);
        save("Five", "20.00", 5);
        save("Ten", "30.00", 10);

        // порог 4: строго больше 4 -> товары со stock 5 и 10
        List<Product> aboveFour = invoke(productRepository,
                "findByStockQuantityGreaterThan", new Class<?>[]{int.class}, 4);
        assertThat(aboveFour).hasSize(2);
        assertThat(aboveFour).extracting(Product::getStockQuantity).containsExactlyInAnyOrder(5, 10);
        assertThat(aboveFour).extracting(Product::getName).containsExactlyInAnyOrder("Five", "Ten");

        // порог 0: stock строго больше 0 -> 2 товара (товар со stock 0 исключён)
        List<Product> aboveZero = invoke(productRepository,
                "findByStockQuantityGreaterThan", new Class<?>[]{int.class}, 0);
        assertThat(aboveZero).hasSize(2);
        assertThat(aboveZero).extracting(Product::getName).doesNotContain("Zero");

        // порог 10: «строго больше» — товара со stock ровно 10 в результате нет
        List<Product> aboveTen = invoke(productRepository,
                "findByStockQuantityGreaterThan", new Class<?>[]{int.class}, 10);
        assertThat(aboveTen).isEmpty();
    }
}
