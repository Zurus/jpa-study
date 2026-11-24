package com.example.jpalab.tasks.t07;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 07, задание 08. N+1 под пагинацией.
 *
 * Пагинация уменьшает число СТРОК, но не отменяет ленивую инициализацию
 * по каждой строке страницы: N в «N+1» = размер страницы, и на каждой
 * странице N запросов к ассоциациям повторяется (плюс count-запрос).
 * Решение — @EntityGraph/fetch join вместе с Pageable (тема 8).
 */
@DataJpaTest
class TASK08Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void paginationDoesNotFixNPlus1() {
        // 6 товаров / 6 категорий
        for (int i = 1; i <= 6; i++) {
            Category category = categoryRepository.save(
                    Category.builder().name("N8 C" + i).build());
            Product product = Product.builder()
                    .name("N8 P" + i)
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

        // первая страница размера 2: count-запрос + data-запрос = 2
        Page<Product> page = productRepository.findAll(PageRequest.of(0, 2));
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(6);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);

        // доступ к категориям элементов страницы: +N, где N = 2
        for (Product product : page.getContent()) {
            product.getCategory().getName();
        }
        // итого 4
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(4);
    }
}
