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
 * Тема 04, задание 03. GROUP BY через ассоциацию.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("""
 *       SELECT p.category.name, COUNT(p) FROM Product p
 *       GROUP BY p.category.name ORDER BY p.category.name ASC
 *       """)
 * List<Object[]> countByCategoryName();
 * }</pre>
 * Свойство-путь p.category.name без явного JOIN: Hibernate САМ добавляет
 * implicit join к таблице категорий.
 */
@DataJpaTest
class TASK03Test {

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

    @Test
    void groupByAssociationPath() {
        // две категории: "Gadgets" x2, "Books" x1
        Category gadgets = categoryRepository.saveAndFlush(
                Category.builder().name("Gadgets").build());
        Category books = categoryRepository.saveAndFlush(
                Category.builder().name("Books").build());
        save("G1", gadgets);
        save("G2", gadgets);
        save("B1", books);

        List<Object[]> rows = invoke(productRepository,
                "countByCategoryName", new Class<?>[]{});

        // строки ["Books", 1] и ["Gadgets", 2]
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)[0]).isEqualTo("Books");
        assertThat(rows.get(0)[1]).isEqualTo(1L);
        assertThat(rows.get(1)[0]).isEqualTo("Gadgets");
        assertThat(rows.get(1)[1]).isEqualTo(2L);
    }
}
