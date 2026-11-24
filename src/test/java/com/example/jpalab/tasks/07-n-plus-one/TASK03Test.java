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
 * Тема 07, задание 03. Лечение N+1: JOIN FETCH (to-one).
 *
 * `SELECT p FROM Product p JOIN FETCH p.category` — Hibernate собирает
 * сущность и ассоциацию в ОДНОЙ SELECT с join: N+1 исчезает, а
 * `getCategory()` у каждого результата уже инициализирован.
 *
 * Метод добавляется студентом в ProductRepository (имя фиксировано
 * заданием), поэтому вызывается рефлексивно через TaskTestSupport:
 * до реализации тест красный с понятным сообщением.
 */
@DataJpaTest
class TASK03Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void joinFetchLoadsCategoryInSingleQuery() {
        // 5 товаров, 5 разных категорий
        for (int i = 1; i <= 5; i++) {
            Category category = categoryRepository.save(
                    Category.builder().name("N3 C" + i).build());
            Product product = Product.builder()
                    .name("N3 P" + i)
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

        @SuppressWarnings("unchecked")
        List<Product> products = TaskTestSupport.invoke(
                productRepository, "findAllWithCategoryJoined", new Class<?>[0]);
        assertThat(products).hasSize(5);

        // ОДИН prepared statement (JOIN FETCH не плодит запросы)
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        // категории уже инициализированы
        for (Product product : products) {
            assertThat(Hibernate.isInitialized(product.getCategory())).isTrue();
        }

        // доступ в цикле не добавляет ни одного запроса
        long before = statistics.getPrepareStatementCount();
        for (Product product : products) {
            product.getCategory().getName();
        }
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(before);
    }
}
