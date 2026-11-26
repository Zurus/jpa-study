package com.example.jpalab.tasks.t07;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.service.ProductService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 07, задание 08. N+1 под пагинацией.
 *
 * Пагинация уменьшает число СТРОК, но не отменяет ленивую инициализацию
 * по каждой строке страницы: N в «N+1» = размер страницы, и на каждой
 * странице N запросов к ассоциациям повторяется (плюс count-запрос).
 * Решение — @EntityGraph/fetch join вместе с Pageable (тема 8).
 *
 * Страница с «наивным» доступом к категориям (paged findAll + цикл по
 * getCategory().getName()) возвращает ВАШ метод
 * `List<String> pageProductRowsWithCategoryNames(Pageable pageable)` в
 * `ProductService` (строки вида "<name> [<categoryName>]") — до его
 * реализации тест красный.
 */
@DataJpaTest
@Import(ProductService.class)
class TASK08Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductService productService;

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

        // первая страница размера 2 (сортировка по id): count-запрос +
        // data-запрос = 2, затем доступ к категориям элементов страницы: +2
        List<String> rows = TaskTestSupport.invoke(productService,
                "pageProductRowsWithCategoryNames",
                new Class<?>[]{Pageable.class},
                PageRequest.of(0, 2, Sort.by("id")));
        assertThat(rows).containsExactly("N8 P1 [N8 C1]", "N8 P2 [N8 C2]");

        // итого 4
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(4);
    }
}
