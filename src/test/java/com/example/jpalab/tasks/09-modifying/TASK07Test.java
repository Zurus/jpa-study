package com.example.jpalab.tasks.t09;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 09, задание 07. flushAutomatically: bulk-запрос видит НЕзаflushенные изменения.
 *
 * Существующий метод updateStockForExpensive(stock, minPrice) объявлен с
 * flushAutomatically = true: перед выполнением bulk UPDATE Hibernate сначала
 * выгружает pending-изменения из PC в БД. Поэтому bulk WHERE price >= :minPrice
 * видит цену, изменённую «в памяти», а не старую цену из БД.
 */
@DataJpaTest
class TASK07Test {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void flushAutomaticallySeesUnflushedChanges() {
        Product a = productRepository.save(Product.builder()
                .name("A").price(new BigDecimal("100.00")).stockQuantity(5).build());
        productRepository.save(Product.builder()
                .name("B").price(new BigDecimal("200.00")).stockQuantity(6).build());
        em.flush();

        // A теперь «грязный» в PC: цена 300.00, но em.flush() не вызывали —
        // в БД всё ещё 100.00
        Product loadedA = productRepository.findById(a.getId()).orElseThrow();
        loadedA.setPrice(new BigDecimal("300.00"));

        // flushAutomatically = true: pending-изменение ушло в БД ПЕРЕД bulk UPDATE,
        // поэтому WHERE price >= 250 видит A.price = 300 (а не 100 из БД)
        int zeroed = productRepository.updateStockForExpensive(0, new BigDecimal("250.00"));
        assertThat(zeroed).isEqualTo(1);

        // в БД: A — stock 0, B — не тронут (200 < 250)
        List<Object[]> rows = em.createNativeQuery(
                "SELECT name, stock_quantity FROM products ORDER BY name", Object[].class)
                .getResultList();
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)[0]).isEqualTo("A");
        assertThat((Integer) rows.get(0)[1]).isZero();
        assertThat(rows.get(1)[0]).isEqualTo("B");
        assertThat((Integer) rows.get(1)[1]).isEqualTo(6);
    }
}
