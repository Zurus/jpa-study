package com.example.jpalab.tasks.t07;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.service.ProductService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

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
 *
 * Наивный цикл по всем товарам реализует ВАШ метод
 * `List<String> allProductCategoryNames()` в `ProductService` (findAll +
 * цикл getCategory().getName(), строки вида "<name> [<categoryName>]")
 * — до его реализации тест красный.
 */
@DataJpaTest(properties = "spring.jpa.properties.hibernate.default_batch_fetch_size=2")
@Import(ProductService.class)
class TASK06Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductService productService;

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

        // ВАШ метод: 1 SELECT по products, инициализация 3 уникальных
        // категорий — батчами по 2 id: 2 prepared statement вместо 3
        List<String> rows = TaskTestSupport.invoke(productService,
                "allProductCategoryNames", new Class<?>[]{});
        assertThat(rows).hasSize(6);
        assertThat(rows).contains("N6 C0 P0 [N6 C0]").contains("N6 C2 P0 [N6 C2]");

        // итого 1 + 2 = 3
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);
    }
}
