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
 * Тема 04, задание 02. Сортировка по агрегату.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("""
 *       SELECT p.name, COUNT(p) FROM Product p
 *       GROUP BY p.name
 *       ORDER BY COUNT(p) DESC, p.name ASC
 *       """)
 * List<Object[]> countByNameByGroupSizeDesc();
 * }</pre>
 * ORDER BY сгруппированного запроса допускает агрегаты: самая большая
 * группа первой, при равных размерах — по алфавиту.
 */
@DataJpaTest
class TASK02Test {

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
    void orderGroupsBySizeDescThenName() {
        // 3 группы разного размера: "Bbb" x3, "Aaa" x1, "Ccc" x1
        save("Aaa");
        save("Bbb");
        save("Bbb");
        save("Bbb");
        save("Ccc");

        List<Object[]> rows = invoke(productRepository,
                "countByNameByGroupSizeDesc", new Class<?>[]{});

        // порядок: [Bbb(3), Aaa(1), Ccc(1)]
        assertThat(rows).hasSize(3);
        assertThat(rows.get(0)[0]).isEqualTo("Bbb");
        assertThat(rows.get(0)[1]).isEqualTo(3L);
        assertThat(rows.get(1)[0]).isEqualTo("Aaa");
        assertThat(rows.get(1)[1]).isEqualTo(1L);
        assertThat(rows.get(2)[0]).isEqualTo("Ccc");
        assertThat(rows.get(2)[1]).isEqualTo(1L);
    }
}
