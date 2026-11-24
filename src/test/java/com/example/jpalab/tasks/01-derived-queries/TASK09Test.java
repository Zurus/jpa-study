package com.example.jpalab.tasks.t01;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
 * Тема 01, задание 09. N+1: derived query + LAZY-ассоциация.
 *
 * Целевой метод (добавить в ProductRepository, без @Query):
 * <pre>{@code
 * List<Product> findByPriceGreaterThan(BigDecimal minPrice);
 * }</pre>
 *
 * Derived query не умеет fetch join: сам запрос выполняет 1 SELECT,
 * а первый доступ к LAZY-категории каждого товара в цикле добавляет
 * отдельный SELECT (1 + N = 4 для 3 товаров).
 * Инструменты тем 02/07 (JOIN FETCH, @EntityGraph) с этим справляются.
 */
@DataJpaTest
class TASK09Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void derivedQueryWithLazyCategoryCausesNPlusOne() {
        // 3 товара, каждый в СВОЕЙ категории (иначе прокси закэшируется)
        String[][] data = {
                {"Prod A", "100.00", "Cat A"},
                {"Prod B", "200.00", "Cat B"},
                {"Prod C", "300.00", "Cat C"},
        };
        for (String[] row : data) {
            Category category = categoryRepository.saveAndFlush(
                    Category.builder().name(row[2]).build());
            productRepository.save(Product.builder()
                    .name(row[0])
                    .price(new BigDecimal(row[1]))
                    .stockQuantity(1)
                    .category(category)
                    .build());
        }
        // flush + clear: данные в БД, persistence context пуст —
        // иначе категории останутся managed, и Hibernate не выполнит
        // доп. SELECT при обращении к ним (N+1 не проявится)
        em.flush();
        em.clear();

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        List<Product> products = invoke(productRepository,
                "findByPriceGreaterThan", new Class<?>[]{BigDecimal.class},
                new BigDecimal("50.00"));
        assertThat(products).hasSize(3);

        // сам запрос — ровно 1 SELECT
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        // доступ к LAZY-категории в цикле: +N SELECT (по одному на товар)
        for (Product product : products) {
            product.getCategory().getName();
        }
        // всего 1 + 3 = 4
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(4);
    }
}
