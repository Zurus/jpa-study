package com.example.jpalab.tasks.t03;

import com.example.jpalab.dto.ProductNamePrice;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 03, задание 03. Проекция в Object[] (native).
 *
 * Студент добавляет в `ProductRepository` native-метод
 * `List<Object[]> nativeIdNamePriceAbove(BigDecimal minPrice)`:
 * столбцы в строке — id (Long), name (String), price (BigDecimal),
 * строго больше :minPrice, порядок — по имени asc, затем id asc.
 * До реализации метода тест красный.
 * Тест вручную маппит строки в record ProductNamePrice.
 */
@DataJpaTest
class TASK03Test {

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
    void objectArrayRowsMapManuallyToRecord() {
        // 3 товара
        save("Alpha", "100.00");
        save("Beta", "200.00");
        save("Gamma", "300.00");

        List<Object[]> rows = TaskTestSupport.invoke(productRepository, "nativeIdNamePriceAbove",
                new Class<?>[]{BigDecimal.class}, new BigDecimal("150.00"));

        // каждая строка — массив из 3 элементов: Long, String, BigDecimal
        assertThat(rows).hasSize(2);
        for (Object[] row : rows) {
            assertThat(row).hasSize(3);
            assertThat(row[0]).isInstanceOf(Long.class);
            assertThat(row[1]).isInstanceOf(String.class);
            assertThat(row[2]).isInstanceOf(BigDecimal.class);
        }

        // порядок строк — по имени
        assertThat(rows).extracting(row -> (String) row[1])
                .containsExactly("Beta", "Gamma");

        // ручной маппинг в record: row -> new ProductNamePrice(row[1], row[2])
        List<ProductNamePrice> mapped = rows.stream()
                .map(row -> new ProductNamePrice((String) row[1], (BigDecimal) row[2]))
                .collect(Collectors.toList());
        assertThat(mapped).extracting(ProductNamePrice::name)
                .containsExactly("Beta", "Gamma");
        assertThat(mapped.get(0).price()).isEqualByComparingTo("200.00");
        assertThat(mapped.get(1).price()).isEqualByComparingTo("300.00");
    }
}
