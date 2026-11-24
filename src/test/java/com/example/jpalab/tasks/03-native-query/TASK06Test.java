package com.example.jpalab.tasks.t03;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
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
 * Тема 03, задание 06. Native + пагинация (Pageable).
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query(value = "SELECT * FROM products ORDER BY name ASC, id ASC", nativeQuery = true)
 * Page<Product> findNativePage(Pageable pageable);
 * }</pre>
 * 5 товаров, pageSize = 2: страницы 2/2/1.
 *
 * Важно: текст native-запроса НЕ содержит LIMIT/OFFSET —
 * лимитирование делает JDBC (setFirstResult/setMaxResults): в SQL-логе
 * виден чистый «SELECT * FROM products ORDER BY name ASC, id ASC».
 */
@DataJpaTest
class TASK06Test {

    @Autowired
    private ProductRepository productRepository;

    private Page<Product> call(int page) {
        return invoke(productRepository,
                "findNativePage", new Class<?>[]{Pageable.class},
                PageRequest.of(page, 2, Sort.unsorted()));
    }

    @Test
    void nativePageable() {
        // 5 товаров (имена задают детерминированный порядок)
        String[] names = {"E Five", "D Four", "C Three", "B Two", "A One"};
        for (int i = 0; i < names.length; i++) {
            productRepository.save(Product.builder()
                    .name(names[i])
                    .price(new BigDecimal((i + 1) + ".00"))
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
        assertThat(page0.getContent()).extracting(Product::getName)
                .doesNotContainAnyElementsOf(page2.getContent().stream()
                        .map(Product::getName).toList());
    }
}
