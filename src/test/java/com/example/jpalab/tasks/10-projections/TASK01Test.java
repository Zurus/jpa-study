package com.example.jpalab.tasks.t10;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 10, задание 01. Проекция полей: скалярный кортеж (name, price).
 *
 * Вместо полных сущностей запрос возвращает только два столбца —
 * List<Object[]>, порядок элементов = порядок в SELECT, сортировка по цене
 * desc с тай-брейком по id.
 */
@DataJpaTest
class TASK01Test {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void scalarTupleProjectionOrderByPriceDesc() {
        productRepository.save(Product.builder()
                .name("Cheap").price(new BigDecimal("50.00")).stockQuantity(1).build());
        productRepository.save(Product.builder()
                .name("Boundary").price(new BigDecimal("100.00")).stockQuantity(1).build());
        productRepository.save(Product.builder()
                .name("Mid").price(new BigDecimal("150.00")).stockQuantity(1).build());
        productRepository.save(Product.builder()
                .name("High B").price(new BigDecimal("250.00")).stockQuantity(1).build());
        productRepository.save(Product.builder()
                .name("High A").price(new BigDecimal("250.00")).stockQuantity(1).build());

        List<Object[]> rows = TaskTestSupport.invoke(productRepository, "namesAndPricesAbove",
                new Class<?>[]{BigDecimal.class}, new BigDecimal("100.00"));

        // строго больше 100: "Boundary" (100.00) не попадает
        assertThat(rows).hasSize(3);

        // в каждой строке ровно 2 столбца в порядке SELECT: [0] name, [1] price
        for (Object[] row : rows) {
            assertThat(row).hasSize(2);
            assertThat(row[0]).isInstanceOf(String.class);
            assertThat(row[1]).isInstanceOf(BigDecimal.class);
        }

        // цена desc; при равной цене — по id: "High B" (id 4) раньше "High A" (id 5)
        assertThat(rows)
                .extracting(row -> row[0])
                .containsExactly("High B", "High A", "Mid");
        assertThat((BigDecimal) rows.get(0)[1]).isEqualByComparingTo("250.00");
        assertThat((BigDecimal) rows.get(1)[1]).isEqualByComparingTo("250.00");
        assertThat((BigDecimal) rows.get(2)[1]).isEqualByComparingTo("150.00");
    }
}
