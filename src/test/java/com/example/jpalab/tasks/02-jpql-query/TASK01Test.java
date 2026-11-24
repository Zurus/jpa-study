package com.example.jpalab.tasks.t02;

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
 * Тема 02, задание 01. Первый JPQL-запрос с именованным параметром.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("SELECT p FROM Product p WHERE p.price > :minPrice")
 * List<Product> findPricedAbove(@Param("minPrice") BigDecimal minPrice);
 * }</pre>
 * Тест на 3 товарах (50/150/250) с двумя разными значениями параметра.
 * Граница не включена (строго «больше»).
 */
@DataJpaTest
class TASK01Test {

    @Autowired
    private ProductRepository productRepository;

    private void save(String name, String price) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .build());
    }

    @Test
    void jpqlNamedQueryFiltersByPrice() {
        // товары 50/150/250
        save("P50", "50.00");
        save("P150", "150.00");
        save("P250", "250.00");

        // вызов с 100 -> 2 товара
        List<Product> above100 = invoke(productRepository,
                "findPricedAbove", new Class<?>[]{BigDecimal.class}, new BigDecimal("100.00"));
        assertThat(above100).extracting(Product::getName)
                .containsExactlyInAnyOrder("P150", "P250");

        // вызов с 200 -> 1 товар
        List<Product> above200 = invoke(productRepository,
                "findPricedAbove", new Class<?>[]{BigDecimal.class}, new BigDecimal("200.00"));
        assertThat(above200).extracting(Product::getName).containsExactly("P250");

        // граница не включена: при пороге ровно 50 товар с ценой 50 НЕ возвращается
        List<Product> above50 = invoke(productRepository,
                "findPricedAbove", new Class<?>[]{BigDecimal.class}, new BigDecimal("50.00"));
        assertThat(above50).extracting(Product::getName)
                .containsExactlyInAnyOrder("P150", "P250");
    }
}
