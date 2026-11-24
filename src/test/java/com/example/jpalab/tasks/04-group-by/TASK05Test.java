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
 * Тема 04, задание 05. HAVING: фильтр по группам.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("""
 *       SELECT c.name, COUNT(p.id) FROM Category c
 *       JOIN c.products p
 *       GROUP BY c.name HAVING COUNT(p.id) >= :minProducts
 *       ORDER BY c.name ASC
 *       """)
 * List<Object[]> categoryCountsAtLeast(@Param("minProducts") int minProducts);
 * }</pre>
 * WHERE фильтрует строки ДО группировки, HAVING — группы ПОСЛЕ агрегации;
 * «WHERE COUNT(...) >= 2» — ошибка, HAVING — единственный способ.
 */
@DataJpaTest
class TASK05Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private void save(String name, Category category) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal("10.00"))
                .stockQuantity(1)
                .category(category)
                .build());
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> call(int minProducts) {
        return invoke(productRepository, "categoryCountsAtLeast",
                new Class<?>[]{int.class}, minProducts);
    }

    @Test
    void havingFiltersGroups() {
        // "Many" x3, "One" x1, "Solo" x1
        Category many = categoryRepository.saveAndFlush(
                Category.builder().name("Many").build());
        Category one = categoryRepository.saveAndFlush(
                Category.builder().name("One").build());
        Category solo = categoryRepository.saveAndFlush(
                Category.builder().name("Solo").build());
        save("M1", many);
        save("M2", many);
        save("M3", many);
        save("O1", one);
        save("S1", solo);

        // minProducts = 2 — только ["Many", 3]
        List<Object[]> atLeast2 = call(2);
        assertThat(atLeast2).hasSize(1);
        assertThat(atLeast2.get(0)[0]).isEqualTo("Many");
        assertThat(atLeast2.get(0)[1]).isEqualTo(3L);

        // minProducts = 1 — все три группы
        List<Object[]> atLeast1 = call(1);
        assertThat(atLeast1).hasSize(3);
        assertThat(atLeast1).extracting(row -> (String) row[0])
                .containsExactly("Many", "One", "Solo");
    }
}
