package com.example.jpalab.tasks.t08;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 08, задание 02. Sort внутри Pageable.
 *
 * Один и тот же метод репозитория (findAll) возвращает разные порядки:
 * Sort — «данные», а не «код» — он едет в Pageable из вызывающего кода,
 * а не захардкожен в SQL метода. Сортировка применяется к ГЛОБАЛЬНОМУ
 * порядку: вторая страница продолжает тот же порядок, а не пересортирует
 * «хвост» заново.
 */
@DataJpaTest
class TASK02Test {

    @Autowired
    private ProductRepository productRepository;

    private void save(String name, String price) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .build());
    }

    @Test
    void sortIsPassedByCallerNotHardcoded() {
        // цены 50, 10, 90, 30, 70
        save("A", "50.00");
        save("B", "10.00");
        save("C", "90.00");
        save("D", "30.00");
        save("E", "70.00");

        // по возрастанию: первая страница — 10, 30, 50
        Page<Product> ascFirst = productRepository.findAll(PageRequest.of(0, 3, Sort.by("price")));
        assertThat(ascFirst.getContent()).extracting(Product::getPrice)
                .containsExactly(new BigDecimal("10.00"), new BigDecimal("30.00"), new BigDecimal("50.00"));

        // по убыванию (тот же метод, другой Sort): 90, 70, 50
        Page<Product> descFirst = productRepository.findAll(
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "price")));
        assertThat(descFirst.getContent()).extracting(Product::getPrice)
                .containsExactly(new BigDecimal("90.00"), new BigDecimal("70.00"), new BigDecimal("50.00"));

        // вторая страница в порядке asc продолжает ГЛОБАЛЬНЫЙ порядок: 70, 90
        // (не «пересортировка хвоста» — хвост {70, 90} и так отсортирован,
        //  но если бы на странице 1 были, например, 80 и 20, asc-страница всё равно была бы [70, 90])
        Page<Product> ascSecond = productRepository.findAll(PageRequest.of(1, 3, Sort.by("price")));
        assertThat(ascSecond.getContent()).extracting(Product::getPrice)
                .containsExactly(new BigDecimal("70.00"), new BigDecimal("90.00"));
        assertThat(ascSecond.isLast()).isTrue();
    }
}
