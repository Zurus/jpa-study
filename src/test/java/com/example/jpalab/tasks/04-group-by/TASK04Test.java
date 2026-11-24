package com.example.jpalab.tasks.t04;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 04, задание 04. Несколько агрегатов в одной строке.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("""
 *       SELECT c.name, COUNT(p.id), SUM(p.price), MIN(p.price), MAX(p.price)
 *       FROM Category c LEFT JOIN c.products p
 *       GROUP BY c.name ORDER BY c.name ASC
 *       """)
 * List<Object[]> categoryAggregates();
 * }</pre>
 */
@DataJpaTest
class TASK04Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private void save(String name, String price, Category category) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .category(category)
                .build());
    }

    @Test
    void multipleAggregatesPerGroup() {
        // "A": цены 10, 20; "B": цена 5
        Category a = categoryRepository.saveAndFlush(Category.builder().name("A").build());
        Category b = categoryRepository.saveAndFlush(Category.builder().name("B").build());
        save("A1", "10.00", a);
        save("A2", "20.00", a);
        save("B1", "5.00", b);

        List<Object[]> rows = invoke(productRepository, "categoryAggregates", new Class<?>[]{});

        assertThat(rows).hasSize(2);

        // A: [A, 2, 30, 10, 20]
        assertThat(rows.get(0)[0]).isEqualTo("A");
        assertThat(rows.get(0)[1]).isEqualTo(2L);
        assertThat((BigDecimal) rows.get(0)[2]).isEqualByComparingTo("30.00");
        assertThat((BigDecimal) rows.get(0)[3]).isEqualByComparingTo("10.00");
        assertThat((BigDecimal) rows.get(0)[4]).isEqualByComparingTo("20.00");

        // B: [B, 1, 5, 5, 5]
        assertThat(rows.get(1)[0]).isEqualTo("B");
        assertThat(rows.get(1)[1]).isEqualTo(1L);
        assertThat((BigDecimal) rows.get(1)[2]).isEqualByComparingTo("5.00");
        assertThat((BigDecimal) rows.get(1)[3]).isEqualByComparingTo("5.00");
        assertThat((BigDecimal) rows.get(1)[4]).isEqualByComparingTo("5.00");
    }
}
