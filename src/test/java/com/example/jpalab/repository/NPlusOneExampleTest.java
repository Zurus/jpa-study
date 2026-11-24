package com.example.jpalab.repository;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 7. Проблема N+1 и способы её решить: JOIN FETCH и @EntityGraph.
 *
 * N+1: 1 SELECT по списку товаров + по одному SELECT на категорию каждого товара.
 * Важно: чтобы получить именно N+1, у каждого товара — СВОЯ категория,
 * иначе Hibernate кэширует прокси в persistence context и дозагрузок будет меньше.
 */
@DataJpaTest
class NPlusOneExampleTest {

    private static final int PRODUCTS = 3;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    private long preparedStatementCount() {
        return em.unwrap(Session.class).getSessionFactory().getStatistics().getPrepareStatementCount();
    }

    private void clearStatistics() {
        Statistics stats = em.unwrap(Session.class).getSessionFactory().getStatistics();
        assertThat(stats).isNotNull();
        stats.clear();
    }

    /** 3 товара, каждый в своей категории. */
    private void prepareData() {
        String[][] data = {
                {"Headphones", "Audio"},
                {"Monitor", "Video"},
                {"Keyboard", "Peripherals"},
        };
        for (String[] row : data) {
            Category category = categoryRepository.save(Category.builder().name(row[1]).build());
            productRepository.save(Product.builder()
                    .name(row[0]).price(new BigDecimal("100.00")).stockQuantity(1)
                    .category(category).build());
        }
        em.flush();
        em.clear();
    }

    @Test
    void accessingLazyAssociationForEveryRowIsNPlusOne() {
        prepareData();

        clearStatistics();
        List<Product> products = productRepository.findAll();
        // 1) один SELECT по products
        assertThat(preparedStatementCount()).isEqualTo(1);
        assertThat(products).hasSize(PRODUCTS);
        // 2) категории еще не загружены (LAZY)
        for (Product product : products) {
            assertThat(Hibernate.isInitialized(product.getCategory())).isFalse();
        }
        // 3) обращаемся к категории каждого товара: +1 SELECT на товар
        for (Product product : products) {
            assertThat(product.getCategory().getName()).isNotBlank();
        }
        // Итого 1 + N запросов
        assertThat(preparedStatementCount()).isEqualTo(1 + PRODUCTS);
    }

    @Test
    void joinFetchLoadsCategoriesWithSingleQuery() {
        prepareData();

        clearStatistics();
        List<Product> products = productRepository.findAllWithCategoryFetched();

        // 1) единственный SELECT ... JOIN: N+1 исчезает
        assertThat(preparedStatementCount()).isEqualTo(1);
        // 2) все категории инициализированы, доп. запросов при доступе не будет
        assertThat(products)
                .hasSize(PRODUCTS)
                .allSatisfy(p -> assertThat(Hibernate.isInitialized(p.getCategory())).isTrue());
    }

    @Test
    void entityGraphLoadsCategoriesWithSingleQuery() {
        prepareData();

        clearStatistics();
        List<Product> products = productRepository.findAllByOrderByPriceAsc();

        // @EntityGraph(attributePaths = "category") -> fetch join, один SELECT
        assertThat(preparedStatementCount()).isEqualTo(1);
        assertThat(products)
                .hasSize(PRODUCTS)
                .allSatisfy(p -> assertThat(Hibernate.isInitialized(p.getCategory())).isTrue());
    }
}
