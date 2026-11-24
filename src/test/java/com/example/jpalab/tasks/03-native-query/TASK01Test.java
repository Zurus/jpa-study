package com.example.jpalab.tasks.t03;

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
 * Тема 03, задание 01. Первый native-запрос.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query(value = "SELECT * FROM products WHERE price > :minPrice", nativeQuery = true)
 * List<Product> findNativePricedAbove(@Param("minPrice") BigDecimal minPrice);
 * }</pre>
 * «Сырой» SQL против реальной таблицы products; параметр — prepared statement.
 */
@DataJpaTest
class TASK01Test {

    @Autowired
    private ProductRepository productRepository;

    private void save(String name, String price, int stockQuantity) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(stockQuantity)
                .build());
    }

    @Test
    void firstNativeQueryReturnsEntities() {
        // товары 50/150/250
        save("Cheap", "50.00", 1);
        save("Middle", "150.00", 2);
        save("Expensive", "250.00", 3);

        // вызов с 100 -> 2 товара
        List<Product> above100 = invoke(productRepository,
                "findNativePricedAbove", new Class<?>[]{BigDecimal.class}, new BigDecimal("100.00"));

        assertThat(above100).hasSize(2);
        assertThat(above100).extracting(Product::getName)
                .containsExactlyInAnyOrder("Middle", "Expensive");

        // поля сущностей заполнены, включая stockQuantity
        for (Product product : above100) {
            assertThat(product.getId()).isNotNull();
            assertThat(product.getPrice()).isNotNull();
            assertThat(product.getStockQuantity()).isIn(2, 3);
        }
        // граница не включена: при пороге ровно 50 товар с ценой 50 не возвращается
        List<Product> above50 = invoke(productRepository,
                "findNativePricedAbove", new Class<?>[]{BigDecimal.class}, new BigDecimal("50.00"));
        assertThat(above50).extracting(Product::getName)
                .containsExactlyInAnyOrder("Middle", "Expensive");
    }
}
