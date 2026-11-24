package com.example.jpalab.tasks.t02;

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
 * Тема 02, задание 02. Детерминированная сортировка с тай-брейком.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("SELECT p FROM Product p WHERE p.price > :min ORDER BY p.price DESC, p.id ASC")
 * List<Product> findPricedAboveSortedDeterministic(@Param("min") BigDecimal min);
 * }</pre>
 * Два товара с одинаковой ценой расходятся вторичным ключом — id по возрастанию.
 */
@DataJpaTest
class TASK02Test {

    @Autowired
    private ProductRepository productRepository;

    private Product save(String name, String price) {
        return productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .build());
    }

    @Test
    void deterministicOrderWithIdTieBreaker() {
        // цены 100, 100, 200: два товара с одинаковой ценой
        Product a100 = save("First Hundred", "100.00");
        Product b100 = save("Second Hundred", "100.00");
        Product c200 = save("Two Hundred", "200.00");

        List<Product> sorted = invoke(productRepository,
                "findPricedAboveSortedDeterministic", new Class<?>[]{BigDecimal.class},
                new BigDecimal("50.00"));

        // товар с ценой 200 — первый
        assertThat(sorted).hasSize(3);
        assertThat(sorted.get(0).getId()).isEqualTo(c200.getId());
        // два товара с ценой 100 — по возрастанию id (проверяем именно id, а не имя)
        assertThat(sorted.get(1).getId()).isEqualTo(a100.getId());
        assertThat(sorted.get(2).getId()).isEqualTo(b100.getId());

        // стабильность: повторный вызов даёт тот же порядок
        List<Product> sortedAgain = invoke(productRepository,
                "findPricedAboveSortedDeterministic", new Class<?>[]{BigDecimal.class},
                new BigDecimal("50.00"));
        assertThat(sortedAgain).extracting(Product::getId)
                .containsExactly(c200.getId(), a100.getId(), b100.getId());
    }
}
