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
 * Тема 07, задание 05. JOIN FETCH с WHERE и ORDER BY.
 *
 * Фильтр и сортировка пишутся поверх алиаса join-а
 * (`JOIN FETCH p.category c WHERE c.name = :categoryName
 * ORDER BY p.price DESC`) — fetch join остаётся ОДНИМ запросом,
 * доп. запросов не порождают.
 *
 * Метод добавляется студентом в ProductRepository (имя и сигнатура
 * фиксированы заданием), поэтому вызывается рефлексивно.
 */
@DataJpaTest
class TASK05Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void fetchJoinWithFilterAndOrderByCategoryName() {
        // категория A — 2 товара (30, 10); категория B — 1 товар (99)
        Category a = categoryRepository.save(Category.builder().name("N5 A").build());
        Category b = categoryRepository.save(Category.builder().name("N5 B").build());
        productRepository.save(Product.builder()
                .name("N5 A-30").price(BigDecimal.valueOf(30, 2))
                .stockQuantity(1).category(a).build());
        productRepository.save(Product.builder()
                .name("N5 A-10").price(BigDecimal.valueOf(10, 2))
                .stockQuantity(1).category(a).build());
        productRepository.save(Product.builder()
                .name("N5 B-99").price(BigDecimal.valueOf(99, 2))
                .stockQuantity(1).category(b).build());
        em.flush();
        em.clear();

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        @SuppressWarnings("unchecked")
        List<Product> found = TaskTestSupport.invoke(
                productRepository, "findWithCategoryByNameFetched",
                new Class<?>[]{String.class}, "N5 A");

        // отфильтрованы ровно товары категории A, по цене desc (30 перед 10)
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Product::getPrice)
                .containsExactly(BigDecimal.valueOf(30, 2), BigDecimal.valueOf(10, 2));
        for (Product product : found) {
            assertThat(product.getCategory().getName()).isEqualTo("N5 A");
        }

        // ровно 1 prepared statement (фильтр + сортировка в той же SELECT)
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        // категории инициализированы
        for (Product product : found) {
            assertThat(Hibernate.isInitialized(product.getCategory())).isTrue();
        }
    }
}
