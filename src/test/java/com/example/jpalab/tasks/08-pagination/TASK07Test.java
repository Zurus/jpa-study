package com.example.jpalab.tasks.t08;

import com.example.jpalab.dto.ProductDto;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.service.ProductService;
import com.example.jpalab.tasks.TaskTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тема 08, задание 07. Граничные PageRequest: «за концом» и size = 0.
 *
 * Почему API-слою нужны обе защиты:
 * - size валидируется на ВХОДЕ (PageRequest.of бросает IllegalArgumentException
 *   при size <= 0) — битый запрос не уходит в БД;
 * - «за концом» — ВАЛИДНОЕ состояние: пустая страница при корректных
 *   метаданных. Клиент (например, листание вперёд после удаления строк)
 *   должен получить пустой результат, а не 500-ошибку.
 *
 * API-страницу (валидация pageNumber/pageSize, сортировка, маппинг в
 * ProductDto) реализует ВАШ сервисный метод
 * `Page<ProductDto> pageProductsForApi(int pageNumber, int pageSize)` в
 * `ProductService` — до его реализации тест красный.
 */
@DataJpaTest
@Import(ProductService.class)
class TASK07Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    private void save(String name, String price) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .build());
    }

    @Test
    void edgeCasesBeyondEndZeroSizeOversized() {
        for (int i = 1; i <= 5; i++) {
            save("P" + i, (i * 10) + ".00");
        }

        // (a) страница далеко за концом: пусто, но метаданные целы, без ошибки
        Page<ProductDto> beyond = TaskTestSupport.invoke(productService,
                "pageProductsForApi", new Class<?>[]{int.class, int.class}, 100, 2);
        assertThat(beyond.getContent()).isEmpty();
        assertThat(beyond.getTotalElements()).isEqualTo(5);
        assertThat(beyond.getTotalPages()).isEqualTo(3);

        // (b) нулевой size отклонён на валидации ДО запроса
        assertThatThrownBy(() -> TaskTestSupport.invoke(productService,
                "pageProductsForApi", new Class<?>[]{int.class, int.class}, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);

        // (c) «одна большая страница»: все элементы, totalPages == 1
        Page<ProductDto> all = TaskTestSupport.invoke(productService,
                "pageProductsForApi", new Class<?>[]{int.class, int.class}, 0, 5);
        assertThat(all.getContent()).hasSize(5);
        assertThat(all.getTotalPages()).isEqualTo(1);
        assertThat(all.hasNext()).isFalse();
        assertThat(all.isLast()).isTrue();
    }
}
