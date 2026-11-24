package com.example.jpalab.tasks.t03;

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
 * Тема 03, задание 04. JOIN в native SQL.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query(value = """
 *       SELECT p.* FROM products p
 *       JOIN categories c ON p.category_id = c.id
 *       WHERE c.name = :categoryName
 *       """, nativeQuery = true)
 * List<Product> findNativeByCategoryName(@Param("categoryName") String categoryName);
 * }</pre>
 * Связь таблиц описана явно через ON с реальным именем колонки-ключа.
 * INNER JOIN: товар без категории не возвращается.
 */
@DataJpaTest
class TASK04Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void nativeJoinByForeignKey() {
        // "Gadgets" с 2 товарами + товар без категории
        Category gadgets = categoryRepository.saveAndFlush(
                Category.builder().name("Gadgets").build());
        productRepository.save(Product.builder()
                .name("Gadget A").price(new BigDecimal("10.00")).stockQuantity(1)
                .category(gadgets).build());
        productRepository.save(Product.builder()
                .name("Gadget B").price(new BigDecimal("20.00")).stockQuantity(1)
                .category(gadgets).build());
        productRepository.save(Product.builder()
                .name("Loose").price(new BigDecimal("30.00")).stockQuantity(1)
                .build());

        List<Product> found = invoke(productRepository,
                "findNativeByCategoryName", new Class<?>[]{String.class}, "Gadgets");

        // запрос по «Gadgets» возвращает 2 товара
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Product::getName)
                .containsExactlyInAnyOrder("Gadget A", "Gadget B");

        // товар без категории в любом случае не возвращается (INNER JOIN)
        assertThat(found).extracting(Product::getName).doesNotContain("Loose");

        // поля возвращённых сущностей заполнены полностью
        for (Product product : found) {
            assertThat(product.getId()).isNotNull();
            assertThat(product.getPrice()).isNotNull();
            assertThat(product.getStockQuantity()).isEqualTo(1);
        }

        // несуществующая категория — пусто
        List<Product> missing = invoke(productRepository,
                "findNativeByCategoryName", new Class<?>[]{String.class}, "No Such");
        assertThat(missing).isEmpty();
    }
}
