package com.example.jpalab.tasks.t02;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 02, задание 03. Явный JOIN к ассоциации.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("SELECT p FROM Product p JOIN p.category c WHERE c.name = :categoryName")
 * List<Product> findProductsOfCategory(@Param("categoryName") String categoryName);
 * }</pre>
 * INNER JOIN: товар без категории никогда не возвращается.
 * Категория подгружается тем же SELECT (без доп. запроса).
 */
@DataJpaTest
class TASK03Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    private void saveProduct(String name, String price, Category category) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .category(category)
                .build());
    }

    @Test
    void explicitJoinByCategoryName() {
        // "Gadgets" с 2 товарами, пустая категория "Books", товар без категории
        Category gadgets = categoryRepository.saveAndFlush(
                Category.builder().name("Gadgets").build());
        saveProduct("Gadget A", "10.00", gadgets);
        saveProduct("Gadget B", "20.00", gadgets);
        categoryRepository.saveAndFlush(Category.builder().name("Books").build());
        saveProduct("Loose Item", "30.00", null);

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        List<Product> gadgetsProducts = invoke(productRepository,
                "findProductsOfCategory", new Class<?>[]{String.class}, "Gadgets");
        assertThat(gadgetsProducts).extracting(Product::getName)
                .containsExactlyInAnyOrder("Gadget A", "Gadget B");
        // один SELECT с join — дополнительных SELECT по категории нет
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
        // category уже инициализирован (принесён join-ом)
        gadgetsProducts.forEach(p ->
                assertThat(Hibernate.isInitialized(p.getCategory())).isTrue());

        // пустая категория "Books" — INNER JOIN: пусто
        List<Product> books = invoke(productRepository,
                "findProductsOfCategory", new Class<?>[]{String.class}, "Books");
        assertThat(books).isEmpty();

        // товар без категории не возвращается
        assertThat(gadgetsProducts).extracting(Product::getName).doesNotContain("Loose Item");
    }
}
