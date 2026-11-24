package com.example.jpalab.tasks.t01;

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
import java.util.HashSet;
import java.util.Set;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 01, задание 07. Пагинация Pageable в derived query.
 *
 * Целевой метод (добавить в ProductRepository, без @Query):
 * <pre>{@code
 * Page<Product> findByNameLikeIgnoreCase(String namePattern, Pageable pageable);
 * }</pre>
 * Имя совпадает с LIKE-паттерном (без учёта регистра, например «Item%»),
 * последний параметр — Pageable. На 6 товарах: 3 страницы по 2 элемента.
 *
 * SQL-лог: каждый вызов метода выполняет 2 запроса — count (для totalElements)
 * и выборка данных страницы. Проверено через Hibernate Statistics ниже.
 */
@DataJpaTest
class TASK07Test {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void pageableDerivedQuerySplitsItemsIntoPages() {
        // 6 товаров "Item 1".."Item 6", pageSize = 2
        for (int i = 1; i <= 6; i++) {
            productRepository.save(Product.builder()
                    .name("Item " + i)
                    .price(new BigDecimal(i + ".00"))
                    .stockQuantity(1)
                    .build());
        }

        Pageable pageable = PageRequest.of(0, 2, Sort.by("id"));
        Page<Product> firstPage = invoke(productRepository,
                "findByNameLikeIgnoreCase",
                new Class<?>[]{String.class, Pageable.class}, "Item%", pageable);

        // 3 страницы по 2 элемента
        assertThat(firstPage.getTotalElements()).isEqualTo(6);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getContent()).extracting(Product::getName)
                .containsExactly("Item 1", "Item 2");
        assertThat(firstPage.hasNext()).isTrue();

        Page<Product> lastPage = invoke(productRepository,
                "findByNameLikeIgnoreCase",
                new Class<?>[]{String.class, Pageable.class},
                "Item%", PageRequest.of(2, 2, Sort.by("id")));
        assertThat(lastPage.getContent()).hasSize(2);
        assertThat(lastPage.getContent()).extracting(Product::getName)
                .containsExactly("Item 5", "Item 6");
        assertThat(lastPage.hasNext()).isFalse();

        // содержимое страниц 0 и 2 не пересекается
        Set<Long> firstIds = new HashSet<>();
        firstPage.getContent().forEach(p -> firstIds.add(p.getId()));
        Set<Long> lastIds = new HashSet<>();
        lastPage.getContent().forEach(p -> lastIds.add(p.getId()));
        firstIds.retainAll(lastIds);
        assertThat(firstIds).isEmpty();

        // каждый вызов = 2 SQL-запроса: count + данные
        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();
        invoke(productRepository, "findByNameLikeIgnoreCase",
                new Class<?>[]{String.class, Pageable.class},
                "Item%", PageRequest.of(1, 2, Sort.by("id")));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
    }
}
