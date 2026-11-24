package com.example.jpalab.tasks.t08;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 08, задание 09. Пагинация + LAZY: N+1 на странице и лечение @EntityGraph.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @EntityGraph(attributePaths = "category")
 * @Query("SELECT p FROM Product p")
 * Page<Product> findAllWithCategoryGraph(Pageable pageable);
 * }</pre>
 *
 * @EntityGraph + Pageable — стандартное лекарство от N+1 в пагинированных
 * списках: граф добавляет fetch join в data-запрос страницы (категория
 * грузится в той же SELECT), а count остаётся отдельным запросом.
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
    void entityGraphCuresNPlus1OnPage() {
        // 6 товаров, каждый в СВОЕЙ категории (иначе прокси закэшируется)
        for (int i = 1; i <= 6; i++) {
            Category category = categoryRepository.saveAndFlush(
                    Category.builder().name("Cat" + i).build());
            productRepository.save(Product.builder()
                    .name("P" + i)
                    .price(new BigDecimal(i + ".00"))
                    .stockQuantity(1)
                    .category(category)
                    .build());
        }
        // flush + clear: данные в БД, а persistence context пуст —
        // иначе категории останутся managed, и Hibernate вернёт их без
        // доп. SELECT (N+1 не проявится)
        em.flush();
        em.clear();

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();

        // (а) без графа: count + данные + N по одному SELECT на LAZY-категорию
        statistics.clear();
        Page<Product> plainPage = productRepository.findAll(PageRequest.of(0, 2));
        assertThat(plainPage.getTotalElements()).isEqualTo(6);
        for (Product product : plainPage.getContent()) {
            product.getCategory().getName();
        }
        // count(1) + data(1) + 2 товара -> 2 lazy-загрузки = 4
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(4);

        // (б) с @EntityGraph: count + одна data-SELECT с fetch join
        statistics.clear();
        Page<Product> graphPage = invoke(productRepository,
                "findAllWithCategoryGraph",
                new Class<?>[]{Pageable.class},
                PageRequest.of(0, 2));
        assertThat(graphPage.getTotalElements()).isEqualTo(6);
        // count(1) + data c join(1) = 2
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
        // чтение категорий НЕ добавляет запросов: они уже в data-SELECT
        for (Product product : graphPage.getContent()) {
            assertThat(Hibernate.isInitialized(product.getCategory())).isTrue();
            product.getCategory().getName();
        }
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
    }
}
