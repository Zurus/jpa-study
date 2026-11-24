package com.example.jpalab.tasks.t08;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 08, задание 05. Фильтр + сортировка + пагинация вместе.
 *
 * Целевой метод (добавить в ProductRepository, без @Query):
 * <pre>{@code
 * Page<Product> findByNameContainingIgnoreCase(String fragment, Pageable pageable);
 * }</pre>
 *
 * Три роли, один вызов: фрагмент — предикат (LIKE, в count тоже),
 * Sort — в Pageable (только в data-запросе), size/offset — в Pageable.
 */
@DataJpaTest
class TASK05Test {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    private void save(String name, String price) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .build());
    }

    @Test
    void filterSortAndPagingInOneCall() {
        save("Alpha 10", "10.00");
        save("Alpha 30", "30.00");
        save("Alpha 20", "20.00");
        save("Beta 50", "50.00");
        save("Beta 5", "5.00");
        em.flush();
        em.clear();

        // фрагмент в нижнем регистре: поиск без учёта регистра находит 3 «Alpha»
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "price"));
        Page<Product> firstPage = invoke(productRepository,
                "findByNameContainingIgnoreCase",
                new Class<?>[]{String.class, Pageable.class},
                "alpha", pageable);

        // только подмножество «Alpha»: total по фильтру, а не по всем 5
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        // глобальный порядок price desc: 30, 20 — а не пересортировка страницы
        assertThat(firstPage.getContent()).extracting(Product::getName)
                .containsExactly("Alpha 30", "Alpha 20");

        // на каждый вызов: count + данные = 2 SQL
        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();
        Page<Product> secondPage = invoke(productRepository,
                "findByNameContainingIgnoreCase",
                new Class<?>[]{String.class, Pageable.class},
                "alpha", PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "price")));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);

        // продолжение того же глобального порядка: 10
        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(secondPage.getContent()).extracting(Product::getName)
                .containsExactly("Alpha 10");
        assertThat(secondPage.hasNext()).isFalse();
    }
}
