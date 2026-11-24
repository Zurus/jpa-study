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
 * Тема 07, задание 04. Лечение N+1: @EntityGraph.
 *
 * @EntityGraph(attributePaths = "category") — декларативный fetch join
 * поверх derived-запроса: Hibernate сам достраивает JOIN FETCH, писать
 * JOIN в JPQL не нужно. Проверки: одна SELECT с join, предикат по цене
 * работает, все категории инициализированы.
 *
 * Метод добавляется студентом в ProductRepository (имя и сигнатура
 * фиксированы заданием), поэтому вызывается рефлексивно.
 */
@DataJpaTest
class TASK04Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void entityGraphAddsFetchJoinToDerivedQuery() {
        // 5 товаров в 5 категориях; цены 10, 20, 30, 40, 50
        for (int i = 1; i <= 5; i++) {
            Category category = categoryRepository.save(
                    Category.builder().name("N4 C" + i).build());
            Product product = Product.builder()
                    .name("N4 P" + i)
                    .price(BigDecimal.valueOf(i * 10L, 2))
                    .stockQuantity(1)
                    .category(category)
                    .build();
            productRepository.save(product);
        }
        em.flush();
        em.clear();

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        BigDecimal minPrice = BigDecimal.valueOf(25, 2);
        @SuppressWarnings("unchecked")
        List<Product> found = TaskTestSupport.invoke(
                productRepository, "findAllByPriceGreaterThan",
                new Class<?>[]{BigDecimal.class}, minPrice);

        // предикат отфильтровал именно товары дороже 25
        assertThat(found).hasSize(3);
        assertThat(found).extracting(Product::getPrice)
                .containsExactlyInAnyOrder(
                        BigDecimal.valueOf(30, 2),
                        BigDecimal.valueOf(40, 2),
                        BigDecimal.valueOf(50, 2));

        // ОДИН prepared statement (fetch join добавлен к тому же запросу)
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        // все категории в результате инициализированы
        for (Product product : found) {
            assertThat(Hibernate.isInitialized(product.getCategory())).isTrue();
        }

        // доп. SELECT-ов нет
        long before = statistics.getPrepareStatementCount();
        for (Product product : found) {
            product.getCategory().getName();
        }
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(before);
    }
}
