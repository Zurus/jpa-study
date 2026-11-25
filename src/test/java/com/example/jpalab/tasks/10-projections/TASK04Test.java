package com.example.jpalab.tasks.t10;

import com.example.jpalab.dto.ProductDto;
import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 10, задание 04. Конструкторное выражение в обычный класс (не record)
 * с полем из связанной сущности (category.id).
 *
 * SELECT new com.example.jpalab.dto.ProductDto(p.id, p.name, p.price,
 * p.stockQuantity, p.category.id) — конструктор принимает 5 аргументов,
 * последний — из JOIN-поля. У товара без категории categoryId = null.
 */
@DataJpaTest
class TASK04Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void classDtoProjectionWithJoinField() {
        Category a = categoryRepository.saveAndFlush(Category.builder().name("CA").build());
        Category b = categoryRepository.saveAndFlush(Category.builder().name("CB").build());
        Product alpha = productRepository.save(Product.builder()
                .name("Alpha").price(new BigDecimal("100.00")).stockQuantity(1).category(a).build());
        Product beta = productRepository.save(Product.builder()
                .name("Beta").price(new BigDecimal("200.00")).stockQuantity(2).category(b).build());
        Product gamma = productRepository.save(Product.builder()
                .name("Gamma").price(new BigDecimal("300.00")).stockQuantity(3).build());

        // price >= 100: все три; сортировка: цена desc, id asc
        List<ProductDto> rows = TaskTestSupport.invoke(productRepository, "fullDtosForPricedAtLeast",
                new Class<?>[]{BigDecimal.class}, new BigDecimal("100.00"));

        assertThat(rows).hasSize(3);
        assertThat(rows)
                .extracting(ProductDto::getName)
                .containsExactly("Gamma", "Beta", "Alpha");

        // у каждого DTO заполнены все 5 полей конструктора
        assertThat(rows.get(0).getId()).isEqualTo(gamma.getId());
        assertThat(rows.get(0).getPrice()).isEqualByComparingTo("300.00");
        assertThat(rows.get(0).getStockQuantity()).isEqualTo(3);
        assertThat(rows.get(0).getCategoryId()).isNull(); // Gamma без категории
        assertThat(rows.get(1).getId()).isEqualTo(beta.getId());
        assertThat(rows.get(1).getCategoryId()).isEqualTo(b.getId());
        assertThat(rows.get(2).getId()).isEqualTo(alpha.getId());
        assertThat(rows.get(2).getCategoryId()).isEqualTo(a.getId());

        // ProductDto — обычный класс, не record
        assertThat(rows.get(0).getClass().isRecord()).isFalse();
    }
}
