package com.example.jpalab.tasks.t14;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.service.ProductCriteriaService;
import com.example.jpalab.tasks.TaskTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 14, задание 07. Программная сборка динамического WHERE: список
 * имён (null/пустой — выключен), минимальная цена (null — выключен),
 * флаг «только в наличии» (stock &gt; 0). Активные предикаты объединяются and.
 * Метод-артефакт: ProductCriteriaService.findDynamic(List, BigDecimal, boolean).
 */
@DataJpaTest
@Import(ProductCriteriaService.class)
class TASK07Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCriteriaService service;

    private List<Product> find(Object names, BigDecimal minPrice, boolean inStockOnly) {
        return TaskTestSupport.invoke(service, "findDynamic",
                new Class<?>[]{List.class, BigDecimal.class, boolean.class}, names, minPrice, inStockOnly);
    }

    @Test
    void dynamicWhereIsBuiltProgrammatically() {
        Category category = categoryRepository.save(Category.builder().name("Warehouse").build());
        productRepository.save(Product.builder().name("Widget").price(new BigDecimal("10.00"))
                .stockQuantity(5).category(category).build());
        productRepository.save(Product.builder().name("Gadget").price(new BigDecimal("20.00"))
                .stockQuantity(0).category(category).build());
        productRepository.save(Product.builder().name("Gizmo").price(new BigDecimal("30.00"))
                .stockQuantity(7).category(category).build());
        productRepository.save(Product.builder().name("Bolt").price(new BigDecimal("40.00"))
                .stockQuantity(3).category(category).build());

        // всё выключено (пустой список, null, false) — все 4
        assertThat(find(List.of(), null, false)).extracting(Product::getName)
                .containsExactlyInAnyOrder("Widget", "Gadget", "Gizmo", "Bolt");

        // только список имён (точное совпадение)
        assertThat(find(List.of("Widget", "Bolt"), null, false)).extracting(Product::getName)
                .containsExactlyInAnyOrder("Widget", "Bolt");

        // только цена
        assertThat(find(null, new BigDecimal("30.00"), false)).extracting(Product::getName)
                .containsExactlyInAnyOrder("Gizmo", "Bolt");

        // только «в наличии» (stock > 0)
        assertThat(find(null, null, true)).extracting(Product::getName)
                .containsExactlyInAnyOrder("Widget", "Gizmo", "Bolt");

        // имена + «в наличии»: Gadget исчерпан — пусто
        assertThat(find(List.of("Gadget"), null, true)).isEmpty();
    }
}
