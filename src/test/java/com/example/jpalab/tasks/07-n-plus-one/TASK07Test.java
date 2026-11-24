package com.example.jpalab.tasks.t07;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
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
 * Тема 07, задание 07. Сравнение: LAZY vs JOIN FETCH vs @EntityGraph.
 *
 * ОДИН тест, три фазы на одних и тех же 4 товарах (4 категории).
 * Перед каждой фазой: em.clear() и сброс статистики — сравнение при
 * равных условиях.
 *
 * «Таблица» (зафиксирована ассертами):
 *   стратегия      | SQL  | когда уместна
 *   LAZY           | 1+N  | ассоциация почти не нужна
 *   JOIN FETCH     | 1    | ассоциация всегда нужна
 *   @EntityGraph   | 1    | декларативный fetch join поверх derived/@Query
 *
 * Фазы 2 и 3 используют методы из заданий 03 и 04 (рефлексивно):
 * пока студент их не добавил, тест красный с понятным сообщением.
 */
@DataJpaTest
class TASK07Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    private Statistics statistics;

    @Test
    void compareLazyFetchJoinAndEntityGraph() {
        // 4 товара / 4 категории (один раз, до фаз); цены все > 0
        for (int i = 1; i <= 4; i++) {
            Category category = categoryRepository.save(
                    Category.builder().name("N7 C" + i).build());
            Product product = Product.builder()
                    .name("N7 P" + i)
                    .price(BigDecimal.valueOf(i * 10L, 2))
                    .stockQuantity(1)
                    .category(category)
                    .build();
            productRepository.save(product);
        }
        em.flush();
        em.clear();
        statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();

        // ── фаза 1: чистый LAZY — 1 + N ──
        statistics.clear();
        List<Product> lazy = productRepository.findAll();
        for (Product product : lazy) {
            product.getCategory().getName();
        }
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(5); // 1 + 4

        // ── фаза 2: JOIN FETCH (задание 03) — 1 ──
        em.clear();
        statistics.clear();
        @SuppressWarnings("unchecked")
        List<Product> joined = TaskTestSupport.invoke(
                productRepository, "findAllWithCategoryJoined", new Class<?>[0]);
        assertThat(joined).hasSize(4);
        for (Product product : joined) {
            assertThat(Hibernate.isInitialized(product.getCategory())).isTrue();
            product.getCategory().getName();
        }
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        // ── фаза 3: @EntityGraph (задание 04) — 1 ──
        em.clear();
        statistics.clear();
        @SuppressWarnings("unchecked")
        List<Product> graphed = TaskTestSupport.invoke(
                productRepository, "findAllByPriceGreaterThan",
                new Class<?>[]{BigDecimal.class}, BigDecimal.ZERO);
        assertThat(graphed).hasSize(4);
        for (Product product : graphed) {
            assertThat(Hibernate.isInitialized(product.getCategory())).isTrue();
            product.getCategory().getName();
        }
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }
}
