package com.example.jpalab.tasks.t10;

import com.example.jpalab.dto.ProductNamePrice;
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
 * Тема 10, задание 03. Record DTO через конструкторное выражение в JPQL.
 *
 * SELECT new com.example.jpalab.dto.ProductNamePrice(p.name, p.price) —
 * конструктор record вызывается прямо в запросе; record даёт сравнение по
 * значениям и доступ к полям через name()/price().
 */
@DataJpaTest
class TASK03Test {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void recordProjectionViaConstructorExpression() {
        productRepository.save(Product.builder()
                .name("P300").price(new BigDecimal("300.00")).stockQuantity(2).build());
        productRepository.save(Product.builder()
                .name("P100").price(new BigDecimal("100.00")).stockQuantity(7).build());
        productRepository.save(Product.builder()
                .name("P200").price(new BigDecimal("200.00")).stockQuantity(0).build());
        productRepository.save(Product.builder()
                .name("P50").price(new BigDecimal("50.00")).stockQuantity(9).build());

        // stock >= 2: P200 (stock 0) не попадает; сортировка по цене asc: 50, 100, 300
        List<ProductNamePrice> rows = TaskTestSupport.invoke(productRepository, "cheapestInStock",
                new Class<?>[]{int.class}, 2);

        assertThat(rows).containsExactly(
                new ProductNamePrice("P50", new BigDecimal("50.00")),
                new ProductNamePrice("P100", new BigDecimal("100.00")),
                new ProductNamePrice("P300", new BigDecimal("300.00")));

        // record: доступ к полям через селекторы и сравнение по значению
        assertThat(rows.get(0).name()).isEqualTo("P50");
        assertThat(rows.get(0).price()).isEqualByComparingTo("50.00");
        assertThat(rows.get(2))
                .isEqualTo(new ProductNamePrice("P300", new BigDecimal("300.00")));
    }
}
