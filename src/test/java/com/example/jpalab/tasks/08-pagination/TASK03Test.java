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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 08, задание 03. Метаданные Page.
 *
 * Из метаданных фронтенд строит «навигацию» (счётчик страниц, кнопки
 * вперёд/назад) без лишних запросов: каждый геттер метаданных —
 * часть контракта Page.
 *
 * Страницу по префиксу имени (без учёта регистра) формирует ВАШ метод
 * репозитория `Page<Product> findByNameStartingWithIgnoreCase(String prefix,
 * Pageable pageable)` — до его реализации тест красный.
 */
@DataJpaTest
class TASK03Test {

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
    void pageMetadataCoversWholeNavigation() {
        // 5 Gadget (1..5) + 2 Widget: префикс "gadget" (без учёта регистра)
        // даёт ровно 5 товаров, size = 2 -> 3 страницы
        for (int i = 1; i <= 5; i++) {
            save("Gadget" + i, (i * 10) + ".00");
        }
        save("Widget1", "1.00");
        save("Widget2", "2.00");

        Page<Product> p0 = TaskTestSupport.invoke(productRepository,
                "findByNameStartingWithIgnoreCase",
                new Class<?>[]{String.class, Pageable.class}, "GADGET", PageRequest.of(0, 2));
        assertThat(p0.getContent()).extracting(Product::getName)
                .containsOnlyElementsOf(List.of("Gadget1", "Gadget2", "Gadget3", "Gadget4", "Gadget5"));
        assertThat(p0.getNumber()).isZero();
        assertThat(p0.getSize()).isEqualTo(2);
        assertThat(p0.getTotalElements()).isEqualTo(5);
        assertThat(p0.getTotalPages()).isEqualTo(3);
        assertThat(p0.getNumberOfElements()).isEqualTo(2);
        assertThat(p0.isFirst()).isTrue();
        assertThat(p0.hasPrevious()).isFalse();
        assertThat(p0.hasNext()).isTrue();
        assertThat(p0.isLast()).isFalse();
        assertThat(p0.isEmpty()).isFalse();

        Page<Product> p1 = TaskTestSupport.invoke(productRepository,
                "findByNameStartingWithIgnoreCase",
                new Class<?>[]{String.class, Pageable.class}, "gadget", PageRequest.of(1, 2));
        assertThat(p1.getNumber()).isEqualTo(1);
        assertThat(p1.getNumberOfElements()).isEqualTo(2);
        assertThat(p1.isFirst()).isFalse();
        assertThat(p1.hasPrevious()).isTrue();
        assertThat(p1.hasNext()).isTrue();
        assertThat(p1.isLast()).isFalse();

        // последняя страница: один элемент
        Page<Product> p2 = TaskTestSupport.invoke(productRepository,
                "findByNameStartingWithIgnoreCase",
                new Class<?>[]{String.class, Pageable.class}, "gadget", PageRequest.of(2, 2));
        assertThat(p2.getNumber()).isEqualTo(2);
        assertThat(p2.getNumberOfElements()).isEqualTo(1);
        assertThat(p2.isLast()).isTrue();
        assertThat(p2.hasNext()).isFalse();
        assertThat(p2.hasPrevious()).isTrue();

        // запрос «за концом» страниц: пустая страница при прежних метаданных
        Page<Product> beyond = TaskTestSupport.invoke(productRepository,
                "findByNameStartingWithIgnoreCase",
                new Class<?>[]{String.class, Pageable.class}, "gadget", PageRequest.of(5, 2));
        assertThat(beyond.isEmpty()).isTrue();
        assertThat(beyond.getContent()).isEmpty();
        assertThat(beyond.getTotalElements()).isEqualTo(5);
        assertThat(beyond.getTotalPages()).isEqualTo(3);
        assertThat(beyond.isLast()).isTrue();
        assertThat(beyond.hasNext()).isFalse();
    }
}
