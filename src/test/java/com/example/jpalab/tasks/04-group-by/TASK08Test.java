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
 * Тема 04, задание 08. NULL и COUNT: пустая группа.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("""
 *       SELECT c.name, COUNT(*), COUNT(p.id), COUNT(c.name), SUM(p.price)
 *       FROM Category c LEFT JOIN c.products p
 *       GROUP BY c.name ORDER BY c.name ASC
 *       """)
 * List<Object[]> nullAggregateRows();
 * }</pre>
 * Различия: COUNT(*) считает строки, COUNT(col) — только ненулевые
 * значения колонки, SUM без строк — NULL (а не 0).
 */
@DataJpaTest
class TASK08Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void nullAggregatesInEmptyGroup() {
        // "Empty" x0 и "Full" x1 (цена 7.50)
        categoryRepository.saveAndFlush(Category.builder().name("Empty").build());
        Category full = categoryRepository.saveAndFlush(
                Category.builder().name("Full").build());
        productRepository.save(Product.builder()
                .name("F1")
                .price(new BigDecimal("7.50"))
                .stockQuantity(1)
                .category(full)
                .build());

        List<Object[]> rows = invoke(productRepository, "nullAggregateRows", new Class<?>[]{});

        assertThat(rows).hasSize(2);

        // пустая группа "Empty":
        // COUNT(*) = 1 (строка существует), COUNT(p.id) = 0 (нет ненулевых id
        // товара), COUNT(c.name) = 1, SUM(p.price) = null
        assertThat(rows.get(0)[0]).isEqualTo("Empty");
        assertThat(rows.get(0)[1]).isEqualTo(1L);
        assertThat(rows.get(0)[2]).isEqualTo(0L);
        assertThat(rows.get(0)[3]).isEqualTo(1L);
        assertThat(rows.get(0)[4]).isNull();

        // "Full": 1, 1, 1, 7.50
        assertThat(rows.get(1)[0]).isEqualTo("Full");
        assertThat(rows.get(1)[1]).isEqualTo(1L);
        assertThat(rows.get(1)[2]).isEqualTo(1L);
        assertThat(rows.get(1)[3]).isEqualTo(1L);
        assertThat((BigDecimal) rows.get(1)[4]).isEqualByComparingTo("7.50");
    }
}
