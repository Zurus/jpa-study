package com.example.jpalab.tasks.t02;

import com.example.jpalab.dto.ProductNamePrice;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 02, задание 06. Constructor expression (DTO-проекция).
 *
 * Целевой метод УЖЕ есть в ProductRepository (используем его):
 * <pre>{@code
 * @Query("""
 *       SELECT new com.example.jpalab.dto.ProductNamePrice(p.name, p.price)
 *       FROM Product p
 *       WHERE p.price >= :minPrice
 *       ORDER BY p.name ASC, p.id ASC
 *       """)
 * List<ProductNamePrice> namePricesAtLeast(@Param("minPrice") BigDecimal minPrice);
 * }</pre>
 * Тест проверяет содержимое record-ов.
 *
 * Ловушка (комментарий): если в `new ...` указать неверное полное имя класса,
 * контекст Spring не поднимется — ошибка будет на старте приложения,
 * а не в момент вызова метода.
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

    @Test
    void constructorExpressionProjectsNameAndPrice() {
        // 3 товара
        save("A One", "100.00");
        save("B Two", "200.00");
        save("C Three", "300.00");

        // возвращаемый тип — список record-ов (не Object[])
        List<ProductNamePrice> result = productRepository.namePricesAtLeast(
                new BigDecimal("150.00"));

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(item -> assertThat(item).isInstanceOf(ProductNamePrice.class));

        // ORDER BY p.name ASC
        assertThat(result).extracting(ProductNamePrice::name)
                .containsExactly("B Two", "C Three");

        // поля record заполнены корректно
        assertThat(result.get(0).price()).isEqualByComparingTo("200.00");
        assertThat(result.get(1).price()).isEqualByComparingTo("300.00");
    }
}
