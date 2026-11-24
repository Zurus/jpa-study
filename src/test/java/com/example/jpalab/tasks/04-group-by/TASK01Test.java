package com.example.jpalab.tasks.t04;

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
 * Тема 04, задание 01. Первый GROUP BY.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("""
 *       SELECT p.name, COUNT(p) FROM Product p
 *       GROUP BY p.name ORDER BY p.name ASC
 *       """)
 * List<Object[]> countByName();
 * }</pre>
 * Правило: каждая колонка SELECT без агрегата обязательна в GROUP BY.
 */
@DataJpaTest
class TASK01Test {

    @Autowired
    private ProductRepository productRepository;

    private void save(String name) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal("10.00"))
                .stockQuantity(1)
                .build());
    }

    @Test
    void groupCountsByName() {
        // два товара одного имени и один с другим (name без unique)
        save("Alpha");
        save("Alpha");
        save("Beta");

        List<Object[]> rows = invoke(productRepository, "countByName", new Class<?>[]{});

        // 2 строки: первая ["Alpha", 2], вторая ["Beta", 1]
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)[0]).isEqualTo("Alpha");
        assertThat(rows.get(1)[0]).isEqualTo("Beta");

        // количество — Long
        assertThat(rows.get(0)[1]).isInstanceOf(Long.class);
        assertThat(rows.get(0)[1]).isEqualTo(2L);
        assertThat(rows.get(1)[1]).isEqualTo(1L);
    }
}
