package com.example.jpalab.tasks.t03;

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
 * Тема 03, задание 10. Native-запрос и LAZY-ассоциации: fetch невозможен.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query(value = "SELECT * FROM products ORDER BY id ASC", nativeQuery = true)
 * List<Product> findNativeAll();
 * }</pre>
 * «Вытащить» категорию вместе с товаром внутри одного native-SELECT нельзя:
 * сам native-SELECT — 1 запрос, доступ к LAZY-категориям добавляет N запросов.
 * Пути против N+1 — JPQL JOIN FETCH / @EntityGraph (темы 02/07),
 * но они не применимы «внутри» native-запроса.
 */
@DataJpaTest
class TASK10Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void nativeCannotFetchLazyAssociations() {
        // 3 товара, каждый в СВОЕЙ категории
        String[][] data = {
                {"Prod A", "Cat A"},
                {"Prod B", "Cat B"},
                {"Prod C", "Cat C"},
        };
        for (String[] row : data) {
            Category category = categoryRepository.saveAndFlush(
                    Category.builder().name(row[1]).build());
            productRepository.save(Product.builder()
                    .name(row[0])
                    .price(new BigDecimal("10.00"))
                    .stockQuantity(1)
                    .category(category)
                    .build());
        }

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        // сам native-SELECT — ровно 1 запрос
        List<Product> products = invoke(productRepository,
                "findNativeAll", new Class<?>[]{});
        assertThat(products).hasSize(3);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        // после запроса category ещё прокси (не инициализирована)
        for (Product product : products) {
            assertThat(Hibernate.isInitialized(product.getCategory())).isFalse();
        }

        // цикл доступа к категориям добавляет N SELECT (всего 1 + N)
        for (Product product : products) {
            product.getCategory().getName();
        }
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(4);
    }
}
