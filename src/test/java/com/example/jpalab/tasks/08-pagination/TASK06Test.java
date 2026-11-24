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
import org.springframework.data.domain.Slice;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 08, задание 06. Page vs Slice: цена в SQL.
 *
 * Page  = count + данные = 2 prepared statement.
 * Slice = только данные (fetch first ? rows only, без count) = 1.
 * Slice не знает общего количества (у интерфейса Slice нет getTotalElements)
 * — он отвечает только на «есть ещё страница?». Поэтому Slice выгоден
 * для длинных листингов, где счётчик «всего» фронтенду не нужен:
 * каждый шаг страницы стоит один запрос вместо двух.
 *
 * Методы в репозиторий НЕ добавляются: Slice берётся из уже существующего
 * derived-метода findAllByOrderByPriceAsc(Pageable) (пример темы 07).
 */
@DataJpaTest
class TASK06Test {

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
    void pageCostsTwoQueriesSliceCostsOne() {
        // 4 товара: P1..P4 с ценами 100..400
        for (int i = 1; i <= 4; i++) {
            save("P" + i, (i * 100) + ".00");
        }
        em.flush();
        em.clear();

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();

        // Page: count + данные
        statistics.clear();
        Page<Product> page = productRepository.findAll(PageRequest.of(0, 2));
        long pageSql = statistics.getPrepareStatementCount();
        assertThat(pageSql).isEqualTo(2);

        // Slice: только данные (count-запрос не выполняется)
        statistics.clear();
        Slice<Product> slice = productRepository.findAllByOrderByPriceAsc(PageRequest.of(0, 2));
        long sliceSql = statistics.getPrepareStatementCount();
        assertThat(sliceSql).isEqualTo(1);

        // контент страниц совпадает: P1, P2
        List<Long> pageIds = page.getContent().stream().map(Product::getId).toList();
        List<Long> sliceIds = slice.getContent().stream().map(Product::getId).toList();
        assertThat(sliceIds).containsExactlyElementsOf(pageIds);
        assertThat(slice.getContent()).extracting(Product::getName)
                .containsExactly("P1", "P2");

        // Slice отвечает на «есть ещё?»: 4 товара, страница 0 размера 2
        assertThat(slice.hasNext()).isTrue();
        assertThat(slice.hasPrevious()).isFalse();
    }
}
