package com.example.jpalab.tasks.t04;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 04, задание 06. LEFT JOIN: пустые группы.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("""
 *       SELECT c.name, COUNT(p.id) FROM Category c
 *       LEFT JOIN c.products p
 *       GROUP BY c.name ORDER BY c.name ASC
 *       """)
 * List<Object[]> productCountPerCategory();
 * }</pre>
 * Идите от «родительной» сущности и LEFT JOIN-ьте коллекцию —
 * пустые группы сохранятся; обычный (INNER) JOIN их «съест».
 */
@DataJpaTest
class TASK06Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    private void save(String name, Category category) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal("10.00"))
                .stockQuantity(1)
                .category(category)
                .build());
    }

    @Test
    void leftJoinKeepsEmptyGroups() {
        // "Full" x2, "Empty" x0
        Category full = categoryRepository.saveAndFlush(
                Category.builder().name("Full").build());
        categoryRepository.saveAndFlush(Category.builder().name("Empty").build());
        save("F1", full);
        save("F2", full);

        // LEFT JOIN: ["Empty", 0] и ["Full", 2]
        List<Object[]> rows = invoke(productRepository,
                "productCountPerCategory", new Class<?>[]{});
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)[0]).isEqualTo("Empty");
        assertThat(rows.get(0)[1]).isEqualTo(0L);
        assertThat(rows.get(1)[0]).isEqualTo("Full");
        assertThat(rows.get(1)[1]).isEqualTo(2L);

        // тот же запрос с INNER JOIN: пустая группа исчезает
        @SuppressWarnings("unchecked")
        List<Object[]> innerOnly = em.createQuery("""
                SELECT c.name, COUNT(p.id) FROM Category c
                JOIN c.products p
                GROUP BY c.name ORDER BY c.name ASC
                """, Object[].class)
                .getResultList();
        assertThat(innerOnly).hasSize(1);
        assertThat(innerOnly.get(0)[0]).isEqualTo("Full");
        assertThat(innerOnly.get(0)[1]).isEqualTo(2L);
    }
}
