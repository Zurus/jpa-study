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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 07, задание 02. Измерить N+1 на @OneToMany (LAZY-коллекции).
 *
 * N+1 для коллекций — это N загрузок КОЛЛЕКЦИЙ (по одной на родителя),
 * а не N × размер коллекции: у первой категории 3 товара, у четвёртой 1,
 * а запросов одинаково — по одному на категорию.
 *
 * Сценарий «считать товары по каждой категории» (findAll категорий +
 * цикл getProducts().size()) реализует ВАШ метод
 * `Map<String, Integer> countProductsPerCategory()` в `ProductService`
 * (имя категории -> размер коллекции) — до его реализации тест красный.
 */
@DataJpaTest
@Import(ProductService.class)
class TASK02Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductService productService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void measureNPlus1OnCollections() {
        // 4 категории с разным числом товаров: 3, 2, 1, 1
        int[] sizes = {3, 2, 1, 1};
        for (int i = 0; i < sizes.length; i++) {
            Category category = categoryRepository.save(
                    Category.builder().name("N2 C" + i).build());
            for (int j = 0; j < sizes[i]; j++) {
                Product product = Product.builder()
                        .name("N2 C" + i + " P" + j)
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

        // ВАШ метод: 1 SELECT по categories + 4 SELECT по коллекциям
        Map<String, Integer> counts = TaskTestSupport.invoke(productService,
                "countProductsPerCategory", new Class<?>[]{});
        assertThat(counts).containsExactlyInAnyOrderEntriesOf(Map.of(
                "N2 C0", 3,
                "N2 C1", 2,
                "N2 C2", 1,
                "N2 C3", 1));

        // итого 1 + 4 = 5
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(5);
    }
}
