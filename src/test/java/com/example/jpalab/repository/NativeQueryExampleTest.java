package com.example.jpalab.repository;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 3. Native-запрос: @Query(nativeQuery = true) работает напрямую
 * с реальной таблицей products (имя таблицы и колонок как в БД).
 */
@DataJpaTest
class NativeQueryExampleTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private void saveProduct(String name, String price, int stock, Category category) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(stock)
                .category(category)
                .build());
    }

    @Test
    void nativeQuerySelectsFromRealTable() {
        Category category = categoryRepository.save(Category.builder().name("Gadgets").build());
        saveProduct("Phone A", "500.00", 10, category);
        saveProduct("Smartphone B", "700.00", 5, category);
        saveProduct("Phone D", "300.00", 7, category);

        // Родной SQL: SELECT p.id, p.name, p.price FROM products p ...
        // Результат возвращается строками Object[] в порядке выбранных колонок
        List<Object[]> rows = productRepository.nativeSearchByPriceAbove(new BigDecimal("400.00"));

        assertThat(rows).hasSize(2);

        Object[] first = rows.get(0);
        assertThat(first[0] instanceof Long).as("первая колонка — id").isTrue();
        assertThat(first[1]).isEqualTo("Phone A");
        assertThat((BigDecimal) first[2]).isEqualByComparingTo("500.00");

        Object[] second = rows.get(1);
        assertThat(second[1]).isEqualTo("Smartphone B");
        assertThat((BigDecimal) second[2]).isEqualByComparingTo("700.00");
    }
}
