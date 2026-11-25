package com.example.jpalab.tasks.t09;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

/**
 * Тема 09, задание 04. Несколько SET-полей и выражение в правой части.
 *
 * Bulk UPDATE умеет менять сразу несколько полей (через запятую) и
 * вычислять новое значение из текущего: p.price = p.price + :delta.
 * Всё это одним SQL-вызовом, без загрузки сущностей.
 */
@DataJpaTest
class TASK04Test {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void multipleSetClauses() {
        // товары без категории: цены 10 / 20 / 50, у всех stock = 1
        productRepository.save(Product.builder()
                .name("P1").price(new BigDecimal("10.00")).stockQuantity(1).build());
        productRepository.save(Product.builder()
                .name("P2").price(new BigDecimal("20.00")).stockQuantity(1).build());
        productRepository.save(Product.builder()
                .name("P3").price(new BigDecimal("50.00")).stockQuantity(1).build());
        em.flush();

        // у товаров с ценой < 30: цена += 5 и stock += 1
        Integer updated = TaskTestSupport.invoke(productRepository, "raisePricesAndRestock",
                new Class<?>[]{BigDecimal.class, BigDecimal.class},
                new BigDecimal("5.00"), new BigDecimal("30.00"));
        assertThat(updated).isEqualTo(2);

        List<Object[]> rows = em.createNativeQuery(
                "SELECT price, stock_quantity FROM products ORDER BY price", Object[].class)
                .getResultList();
        assertThat(rows).hasSize(3);
        assertThat(((Number) rows.get(0)[0]).doubleValue()).isCloseTo(15.00, offset(0.001));
        assertThat((Integer) rows.get(0)[1]).isEqualTo(2);
        assertThat(((Number) rows.get(1)[0]).doubleValue()).isCloseTo(25.00, offset(0.001));
        assertThat((Integer) rows.get(1)[1]).isEqualTo(2);
        assertThat(((Number) rows.get(2)[0]).doubleValue()).isCloseTo(50.00, offset(0.001));
        assertThat((Integer) rows.get(2)[1]).isEqualTo(1);
    }
}
