package com.example.jpalab.tasks.t08;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
 * Тема 08, задание 04. Page + фильтрация.
 *
 * Целевой метод (добавить в ProductRepository, без @Query):
 * <pre>{@code
 * Page<Product> findByPriceGreaterThan(BigDecimal minPrice, Pageable pageable);
 * }</pre>
 *
 * «Пагинация по фильтрованному виду»: count и данные обязаны видеть
 * ОДНО И ТО ЖЕ условие. Функциональное доказательство — totalElements:
 * если бы count считал все товары, было бы 6, а не 4 (в SQL-логе
 * count-запрос содержит where price > ?).
 */
@DataJpaTest
class TASK04Test {

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
    void pageCountReflectsPredicate() {
        // цены 10..60; порог 20 -> подходят 4 (30, 40, 50, 60)
        for (int i = 1; i <= 6; i++) {
            save("P" + i, (i * 10) + ".00");
        }
        em.flush();
        em.clear();

        Page<Product> firstPage = invoke(productRepository,
                "findByPriceGreaterThan",
                new Class<?>[]{BigDecimal.class, Pageable.class},
                new BigDecimal("20.00"),
                PageRequest.of(0, 3, Sort.by("price")));

        // count посчитан по ОТФИЛЬТРОВАННОМУ множеству: 4, а не 6
        assertThat(firstPage.getTotalElements()).isEqualTo(4);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getContent()).hasSize(3);
        assertThat(firstPage.getContent()).extracting(Product::getPrice)
                .containsExactly(
                        new BigDecimal("30.00"),
                        new BigDecimal("40.00"),
                        new BigDecimal("50.00"));

        // товар ровно на границе (цена 20) НЕ входит — предикат строгий
        assertThat(firstPage.getContent()).extracting(Product::getName).doesNotContain("P2");

        // вторая страница: оставшийся один товар
        Page<Product> secondPage = invoke(productRepository,
                "findByPriceGreaterThan",
                new Class<?>[]{BigDecimal.class, Pageable.class},
                new BigDecimal("20.00"),
                PageRequest.of(1, 3, Sort.by("price")));
        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(secondPage.getContent()).extracting(Product::getPrice)
                .containsExactly(new BigDecimal("60.00"));
        assertThat(secondPage.hasNext()).isFalse();
    }
}
