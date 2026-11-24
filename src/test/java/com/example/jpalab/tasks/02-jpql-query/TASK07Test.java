package com.example.jpalab.tasks.t02;

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
import java.util.List;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 02, задание 07. JPQL + пагинация Pageable.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("SELECT p FROM Product p WHERE p.price BETWEEN :from AND :to")
 * Page<Product> findPageByPriceBetween(@Param("from") BigDecimal from,
 *                                      @Param("to") BigDecimal to,
 *                                      Pageable pageable);
 * }</pre>
 * 5 товаров с ценами 100..500, pageSize = 2: страницы 2/2/1.
 * Каждый вызов = 2 SQL (count + данные) — проверено через Statistics.
 */
@DataJpaTest
class TASK07Test {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    private Page<Product> call(int page) {
        return invoke(productRepository,
                "findPageByPriceBetween",
                new Class<?>[]{BigDecimal.class, BigDecimal.class, Pageable.class},
                new BigDecimal("100.00"), new BigDecimal("500.00"),
                PageRequest.of(page, 2, Sort.by("id")));
    }

    @Test
    void jpqlPageByPriceBetween() {
        // цены 100..500
        for (int price = 100; price <= 500; price += 100) {
            productRepository.save(Product.builder()
                    .name("Page " + price)
                    .price(new BigDecimal(price + ".00"))
                    .stockQuantity(1)
                    .build());
        }

        Page<Product> page0 = call(0);
        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getTotalElements()).isEqualTo(5);
        assertThat(page0.getTotalPages()).isEqualTo(3);
        assertThat(page0.hasNext()).isTrue();

        Page<Product> page1 = call(1);
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.hasNext()).isTrue();

        Page<Product> page2 = call(2);
        assertThat(page2.getContent()).hasSize(1);
        assertThat(page2.hasNext()).isFalse();

        // страницы не пересекаются
        List<Long> firstIds = page0.getContent().stream().map(Product::getId).toList();
        List<Long> lastIds = page2.getContent().stream().map(Product::getId).toList();
        assertThat(firstIds).isNotEqualTo(lastIds);

        // каждый вызов = 2 SQL: count-запрос + данные
        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();
        call(0);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
    }
}
