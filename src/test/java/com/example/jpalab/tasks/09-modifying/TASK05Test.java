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
import java.util.Map;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 09, задание 05. Коллекционный параметр IN :names в bulk UPDATE.
 *
 * Hibernate расширяет List-параметр в IN :names на нужное число плейсхолдеров:
 * один bulk UPDATE меняет stock у всех товаров из списка.
 */
@DataJpaTest
class TASK05Test {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void inListParameter() {
        // четыре товара без категории, у всех stock = 0
        productRepository.save(Product.builder()
                .name("A").price(new BigDecimal("10.00")).stockQuantity(0).build());
        productRepository.save(Product.builder()
                .name("B").price(new BigDecimal("20.00")).stockQuantity(0).build());
        productRepository.save(Product.builder()
                .name("C").price(new BigDecimal("30.00")).stockQuantity(0).build());
        productRepository.save(Product.builder()
                .name("D").price(new BigDecimal("40.00")).stockQuantity(0).build());
        em.flush();

        Integer updated = TaskTestSupport.invoke(productRepository, "setStockByNames",
                new Class<?>[]{List.class, int.class}, List.of("A", "C"), 7);
        assertThat(updated).isEqualTo(2);

        List<Object[]> rows = em.createNativeQuery(
                "SELECT name, stock_quantity FROM products ORDER BY name", Object[].class)
                .getResultList();
        Map<String, Integer> stocks = new LinkedHashMap<>();
        for (Object[] row : rows) {
            stocks.put((String) row[0], (Integer) row[1]);
        }
        assertThat(stocks).containsEntry("A", 7);
        assertThat(stocks).containsEntry("C", 7);
        assertThat(stocks).containsEntry("B", 0);
        assertThat(stocks).containsEntry("D", 0);
    }
}
