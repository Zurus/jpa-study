package com.example.jpalab.tasks.t08;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 08, задание 01. Первый Page.
 *
 * Page = «данные страницы» + «метаданные обо всём множестве»
 * (totalElements/totalPages). Метаданные требуют отдельного count-запроса
 * (select count(...)), данные страницы — выборки с LIMIT/OFFSET.
 *
 * Страницу с условием формирует ВАШ метод репозитория
 * `Page<Product> findPricedAtLeast(BigDecimal minPrice, Pageable pageable)`
 * (только товары с ценой от minPrice) — до его реализации тест красный.
 */
@DataJpaTest
class TASK01Test {

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
    void firstPageHasContentAndTotalElements() {
        // 5 товаров
        for (int i = 1; i <= 5; i++) {
            save("P" + i, (i * 10) + ".00");
        }
        em.flush();
        em.clear();

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        // товары от 20.00: 20, 30, 40, 50 — всего 4, страница размера 2
        Page<Product> page = TaskTestSupport.invoke(productRepository,
                "findPricedAtLeast", new Class<?>[]{BigDecimal.class, Pageable.class},
                new BigDecimal("20.00"), PageRequest.of(0, 2));

        // Page: ровно 2 SQL — count (для totalElements) + данные страницы
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
        // размер страницы
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getNumberOfElements()).isEqualTo(2);
        // total — все подходящие товары (4), а не только страница
        assertThat(page.getTotalElements()).isEqualTo(4);
    }
}
