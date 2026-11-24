package com.example.jpalab.tasks.t02;

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
 * Тема 02, задание 06. Выборка одного поля: {@code List<String>} имён товаров.
 *
 * Целевой метод (добавляется студентом в {@link ProductRepository}):
 * {@code List<String> findNamesByPriceBetween(@Param("from") BigDecimal from,
 * @Param("to") BigDecimal to)} — JPQL-запрос, возвращающий только имена товаров
 * с ценой строго между :from и :to, по возрастанию имени.
 *
 * До реализации метод отсутствует — тест падает с
 * «Задание не реализовано» (через {@link TaskTestSupport}).
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
    void singleFieldProjectionReturnsOnlyNames() {
        // 4 товара; 100 и 200 — границы диапазона
        save("Alpha", "100.00");
        save("Beta", "150.00");
        save("Gamma", "200.00");
        save("Delta", "250.00");

        // (100, 200): строго между -> только товар с ценой 150
        List<String> mid = TaskTestSupport.invoke(productRepository,
                "findNamesByPriceBetween",
                new Class<?>[] { BigDecimal.class, BigDecimal.class },
                new BigDecimal("100.00"), new BigDecimal("200.00"));
        assertThat(mid).containsExactly("Beta");

        // (99, 251): все 4 товара, отсортированы по имени (Delta < Gamma)
        List<String> all = TaskTestSupport.invoke(productRepository,
                "findNamesByPriceBetween",
                new Class<?>[] { BigDecimal.class, BigDecimal.class },
                new BigDecimal("99.00"), new BigDecimal("251.00"));
        assertThat(all).containsExactly("Alpha", "Beta", "Delta", "Gamma");
    }
}
