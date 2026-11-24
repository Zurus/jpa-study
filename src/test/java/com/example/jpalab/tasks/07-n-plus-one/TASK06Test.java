package com.example.jpalab.tasks.t07;

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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 07, задание 06. Batch fetching: default_batch_fetch_size.
 *
 * Свойство контекста `spring.jpa.properties.hibernate.default_batch_fetch_size=2`
 * заставляет Hibernate собирать инициализации LAZY-ассоциаций в IN-батчи:
 * 3 УНИКАЛЬНЫЕ категории загружаются 2 запросами (IN (?, ?) и IN (?)),
 * а не тремя отдельными SELECT. Повторы категории в цикле запросов
 * не плодят — после первой загрузки сущность кэшируется в PC.
 */
@DataJpaTest(properties = "spring.jpa.properties.hibernate.default_batch_fetch_size=2")
class TASK06Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void defaultBatchFetchSizeCollapsesProxyLoads() {
        // 6 товаров в 3 категориях: 3, 2, 1
        int[] sizes = {3, 2, 1};
        for (int i = 0; i < sizes.length; i++) {
            Category category = categoryRepository.save(
                    Category.builder().name("N6 C" + i).build());
            for (int j = 0; j < sizes[i]; j++) {
                Product product = Product.builder()
                        .name("N6 C" + i + " P" + j)
                        .price(BigDecimal.valueOf(i * 10L + j, 2))
                        .stockQuantity(1)
                        .category(category)
                        .build();
                productRepository.save(product);
            }
        }
        em.flush();
        em.clear();

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        // 1 SELECT по products
        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(6);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        // инициализация 3 уникальных категорий — батчами по 2 id:
        // 2 prepared statement вместо 3 отдельных SELECT
        for (Product product : products) {
            product.getCategory().getName();
        }
        // итого 1 + 2 = 3
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);
    }
}
