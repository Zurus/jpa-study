package com.example.jpalab.tasks.t04;

import com.example.jpalab.dto.ProductNamePrice;
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
 * Тема 04, задание 07. Constructor expression по группам.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("""
 *       SELECT new com.example.jpalab.dto.ProductNamePrice(c.name, SUM(p.price))
 *       FROM Category c LEFT JOIN c.products p
 *       GROUP BY c.name ORDER BY c.name ASC
 *       """)
 * List<ProductNamePrice> categoryNameWithSumOfPrices();
 * }</pre>
 * Агрегат передаётся в конструктор так же, как обычное поле —
 * но пустая группа даёт NULL (record допускает null).
 */
@DataJpaTest
class TASK07Test {

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
    void constructorExpressionOverGroups() {
        // "A": 10 + 20; "B": пусто
        Category a = categoryRepository.saveAndFlush(Category.builder().name("A").build());
        Category b = categoryRepository.saveAndFlush(Category.builder().name("B").build());
        save("A1", "10.00", a);
        save("A2", "20.00", a);

        // результат — список record (не Object[])
        List<ProductNamePrice> rows = invoke(productRepository,
                "categoryNameWithSumOfPrices", new Class<?>[]{});

        assertThat(rows).hasSize(2);

        // A: сумма 30
        assertThat(rows.get(0).name()).isEqualTo("A");
        assertThat(rows.get(0).price()).isNotNull();
        assertThat(rows.get(0).price()).isEqualByComparingTo("30.00");

        // B: пустая группа -> сумма NULL (зафиксировано)
        assertThat(rows.get(1).name()).isEqualTo("B");
        assertThat(rows.get(1).price()).isNull();
    }
}
