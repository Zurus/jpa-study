package com.example.jpalab.tasks.t08;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 08, задание 02. Sort внутри Pageable.
 *
 * Один и тот же метод репозитория возвращает разные порядки: Sort —
 * «данные», а не «код» — он едет в Pageable из вызывающего кода, а не
 * захардкожен в SQL метода. Сортировка применяется к ГЛОБАЛЬНОМУ порядку:
 * вторая страница продолжает тот же порядок, а не пересортирует «хвост»
 * заново.
 *
 * Страницу с условием по остатку формирует ВАШ метод репозитория
 * `Page<Product> findByStockQuantityGreaterThanEqual(int minStock,
 * Pageable pageable)` — до его реализации тест красный.
 */
@DataJpaTest
class TASK02Test {

    @Autowired
    private ProductRepository productRepository;

    private void save(String name, String price, int stock) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(stock)
                .build());
    }

    @Test
    void sortIsPassedByCallerNotHardcoded() {
        // остатки 5, 1, 8, 2, 6 (цены не важны)
        save("A", "1.00", 5);
        save("B", "2.00", 1);
        save("C", "3.00", 8);
        save("D", "4.00", 2);
        save("E", "5.00", 6);

        // товары с остатком >= 2: 5, 8, 2, 6 — 4 шт.; asc по остатку:
        // первая страница — 2, 5, 6
        Page<Product> ascFirst = TaskTestSupport.invoke(productRepository,
                "findByStockQuantityGreaterThanEqual",
                new Class<?>[]{int.class, Pageable.class},
                2, PageRequest.of(0, 3, Sort.by("stockQuantity")));
        assertThat(ascFirst.getContent()).extracting(Product::getStockQuantity)
                .containsExactly(2, 5, 6);

        // по убыванию (тот же метод, другой Sort): 8, 6, 5
        Page<Product> descFirst = TaskTestSupport.invoke(productRepository,
                "findByStockQuantityGreaterThanEqual",
                new Class<?>[]{int.class, Pageable.class},
                2, PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "stockQuantity")));
        assertThat(descFirst.getContent()).extracting(Product::getStockQuantity)
                .containsExactly(8, 6, 5);

        // вторая страница в порядке asc продолжает ГЛОБАЛЬНЫЙ порядок: 8
        Page<Product> ascSecond = TaskTestSupport.invoke(productRepository,
                "findByStockQuantityGreaterThanEqual",
                new Class<?>[]{int.class, Pageable.class},
                2, PageRequest.of(1, 3, Sort.by("stockQuantity")));
        assertThat(ascSecond.getContent()).extracting(Product::getStockQuantity)
                .containsExactly(8);
        assertThat(ascSecond.isLast()).isTrue();
    }
}
