package com.example.jpalab.repository;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
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
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 8. Пагинация: Page, Slice, Pageable, Sort.
 *
 * Page  - 2 SQL: count-запрос + выборка страницы.
 * Slice - 1 SQL: count не считается (вместо него добирают лишний элемент,
 *             чтобы определить hasNext).
 */
@DataJpaTest
class PaginationExampleTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    private long preparedStatementCount() {
        return em.unwrap(Session.class).getSessionFactory().getStatistics().getPrepareStatementCount();
    }

    private void clearStatistics() {
        Statistics stats = em.unwrap(Session.class).getSessionFactory().getStatistics();
        assertThat(stats).isNotNull();
        stats.clear();
    }

    /** 5 товаров с ценами 100..500. */
    private void prepareData() {
        Category category = categoryRepository.save(Category.builder().name("Gadgets").build());
        for (int i = 1; i <= 5; i++) {
            productRepository.save(Product.builder()
                    .name("P" + i)
                    .price(new BigDecimal(i * 100 + ".00"))
                    .stockQuantity(i)
                    .category(category)
                    .build());
        }
    }

    @Test
    void pageReturnsMetadataAndExecutesCountQuery() {
        prepareData();

        clearStatistics();
        Page<Product> page = productRepository.findAll(
                PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "price")));

        // Page: 2 SQL (count + данные страницы)
        assertThat(preparedStatementCount()).isEqualTo(2);
        // Sort по price DESC: самые дорогие на первой странице
        assertThat(page.getContent())
                .extracting(Product::getName)
                .containsExactly("P5", "P4");
        // Метаданные страницы
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getNumberOfElements()).isEqualTo(2);
        assertThat(page.getNumber()).isZero();
        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
        assertThat(page.isLast()).isFalse();
    }

    @Test
    void sliceSkipsCountQuery() {
        prepareData();

        clearStatistics();
        // ДЕРЕВИРОВАННЫЙ slice-метод: только limit+1 строк, count-запрос не выполняется.
        // (Ядро-метод findAll(Pageable) при возврате Slice исполняется через
        // реализацию Page из SimpleJpaRepository и включает count.)
        Slice<Product> slice = productRepository.findAllByOrderByPriceAsc(
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "price")));

        // Slice: всего 1 SQL — count-запрос не выполняется
        assertThat(preparedStatementCount()).isEqualTo(1);
        assertThat(slice.getContent())
                .extracting(Product::getName)
                .containsExactly("P1", "P2");
        assertThat(slice.hasNext()).isTrue();
        assertThat(slice.hasPrevious()).isFalse();
        assertThat(slice.isFirst()).isTrue();
    }

    @Test
    void pageableAddressingOfMiddlePage() {
        prepareData();

        Page<Product> middle = productRepository.findAll(
                PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "price")));

        // Нулевая индексация страниц: средняя из трех страниц
        assertThat(middle.getContent())
                .extracting(Product::getName)
                .containsExactly("P3", "P4");
        assertThat(middle.getNumber()).isEqualTo(1);
        assertThat(middle.isFirst()).isFalse();
        assertThat(middle.isLast()).isFalse();
        assertThat(middle.hasNext()).isTrue();
        assertThat(middle.hasPrevious()).isTrue();

        // Последняя страница короче размера
        List<Product> lastContent = productRepository.findAll(
                        PageRequest.of(2, 2, Sort.by(Sort.Direction.ASC, "price")))
                .getContent();
        assertThat(lastContent)
                .extracting(Product::getName)
                .containsExactly("P5");
    }
}
