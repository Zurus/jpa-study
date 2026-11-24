package com.example.jpalab.tasks.t02;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 02, задание 09. Подзапрос IN.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("""
 *       SELECT p FROM Product p
 *       WHERE p.category IN (SELECT c FROM Category c WHERE c.name LIKE :pattern)
 *       """)
 * List<Product> findProductsInCategoriesLike(@Param("pattern") String pattern);
 * }</pre>
 * «Ловушка имён»: "Notebooks" начинается на "Note", а не на "Book",
 * поэтому паттерн "Book%" её не выбирает.
 */
@DataJpaTest
class TASK09Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final Map<String, Category> categories = new HashMap<>();

    private void saveIn(String categoryName, String productName, String price) {
        Category category = categories.computeIfAbsent(categoryName, name ->
                categoryRepository.saveAndFlush(Category.builder().name(name).build()));
        productRepository.save(Product.builder()
                .name(productName)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .category(category)
                .build());
    }

    @Test
    void inSubqueryByCategoryNamePattern() {
        // категории "Books" и "Bookkeeping" с товарами + "Notebooks" (не подходит)
        saveIn("Books", "Book A", "10.00");
        saveIn("Books", "Book B", "20.00");
        saveIn("Bookkeeping", "Ledger", "30.00");
        saveIn("Notebooks", "Notebook X", "40.00");
        // товар БЕЗ категории
        productRepository.save(Product.builder()
                .name("Free Item").price(new BigDecimal("50.00")).stockQuantity(1).build());

        // паттерн "Book%": товары обеих «книжных» категорий (2 категории -> 3 товара)
        List<Product> books = invoke(productRepository,
                "findProductsInCategoriesLike", new Class<?>[]{String.class}, "Book%");
        assertThat(books).extracting(Product::getName)
                .containsExactlyInAnyOrder("Book A", "Book B", "Ledger");
        // "Notebooks" не попадает под "Book%"
        assertThat(books).extracting(Product::getName).doesNotContain("Notebook X");
        // товар без категории никогда не возвращается (интернал-семантика подзапроса)
        assertThat(books).extracting(Product::getName).doesNotContain("Free Item");

        // паттерн действительно параметр: "N%" находит только "Notebooks"
        List<Product> notebooks = invoke(productRepository,
                "findProductsInCategoriesLike", new Class<?>[]{String.class}, "N%");
        assertThat(notebooks).extracting(Product::getName).containsExactly("Notebook X");
    }
}
