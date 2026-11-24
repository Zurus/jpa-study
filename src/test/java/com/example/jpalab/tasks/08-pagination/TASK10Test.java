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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 08, задание 10. Slice + фильтр + Sort: механика «лишней» строки.
 *
 * Целевой метод (добавить в ProductRepository, без @Query):
 * <pre>{@code
 * Slice<Product> sliceByNameContainingIgnoreCase(String fragment, Pageable pageable);
 * }</pre>
 *
 * Механика hasNext у Slice: драйверу запрашивается size + 1 строк
 * (fetch first ? rows only с параметром size+1). «Лишняя» строка — лишь
 * сигнал «есть ещё»; в контент она НЕ попадает. count-запрос не выполняется:
 * на каждый вызов ровно 1 SQL.
 */
@DataJpaTest
class TASK10Test {

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
    void sliceExtraRowDrivesHasNext() {
        // подходят 3 «Alpha»; «Beta» отфильтрован
        save("Alpha 1", "1.00");
        save("Alpha 2", "2.00");
        save("Alpha 3", "3.00");
        save("Beta", "99.00");
        em.flush();
        em.clear();

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();

        // s0: строк больше страницы (3 > 2) — «лишняя» строка «дотянулась»
        statistics.clear();
        Slice<Product> s0 = invoke(productRepository,
                "sliceByNameContainingIgnoreCase",
                new Class<?>[]{String.class, Pageable.class},
                "alpha", PageRequest.of(0, 2, Sort.by("price")));
        // ровно 1 SQL: count-запроса нет
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
        assertThat(s0.getContent()).hasSize(2);
        assertThat(s0.getContent()).extracting(Product::getName)
                .containsExactly("Alpha 1", "Alpha 2");
        assertThat(s0.hasNext()).isTrue();

        // s1: после смещения остаётся ровно одна строка — «лишняя» не «дотянулась»
        statistics.clear();
        Slice<Product> s1 = invoke(productRepository,
                "sliceByNameContainingIgnoreCase",
                new Class<?>[]{String.class, Pageable.class},
                "alpha", PageRequest.of(1, 2, Sort.by("price")));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
        assertThat(s1.getContent()).hasSize(1);
        assertThat(s1.getContent()).extracting(Product::getName)
                .containsExactly("Alpha 3");
        assertThat(s1.hasNext()).isFalse();
    }
}
