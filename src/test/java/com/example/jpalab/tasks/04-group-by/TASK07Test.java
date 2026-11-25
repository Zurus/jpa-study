package com.example.jpalab.tasks.t04;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

/**
 * Тема 04, задание 07. AVG + HAVING: категории по средней цене.
 *
 * Целевой метод (добавляется студентом в {@link ProductRepository}):
 * {@code List<Object[]> categoryAvgPriceAbove(@Param("minAvg") BigDecimal minAvg)}
 * — JPQL: LEFT JOIN от категории к товарам, GROUP BY по категории,
 * HAVING по агрегату AVG (строгое «больше»), ORDER BY по имени категории.
 *
 * До реализации метод отсутствует — тест падает с
 * «Задание не реализовано» (через {@link com.example.jpalab.tasks.TaskTestSupport}).
 */
@DataJpaTest
class TASK07Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private void save(String name, String price, Category category) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .category(category)
                .build());
    }

    @Test
    @SuppressWarnings("unchecked")
    void avgWithHavingFiltersCategories() {
        // "Books": 50 + 70  -> AVG 60
        // "Gadgets": 100 + 300 -> AVG 200
        // "Empty": нет товаров -> AVG NULL
        Category books = categoryRepository.saveAndFlush(Category.builder().name("Books").build());
        Category gadgets = categoryRepository.saveAndFlush(Category.builder().name("Gadgets").build());
        Category empty = categoryRepository.saveAndFlush(Category.builder().name("Empty").build());
        save("Book One", "50.00", books);
        save("Book Two", "70.00", books);
        save("Gadget One", "100.00", gadgets);
        save("Gadget Two", "300.00", gadgets);

        // (100): строго больше 100 -> только "Gadgets" (AVG 200)
        List<Object[]> top = invoke(productRepository,
                "categoryAvgPriceAbove",
                new Class<?>[] { BigDecimal.class },
                new BigDecimal("100"));
        assertThat(top).hasSize(1);
        assertThat(top.get(0)[0]).isEqualTo("Gadgets");
        assertThat(((Number) top.get(0)[1]).doubleValue()).isCloseTo(200.00, offset(0.001));

        // (50): две категории, порядок по имени
        List<Object[]> both = invoke(productRepository,
                "categoryAvgPriceAbove",
                new Class<?>[] { BigDecimal.class },
                new BigDecimal("50"));
        assertThat(both).hasSize(2);
        assertThat(both.get(0)[0]).isEqualTo("Books");
        assertThat(((Number) both.get(0)[1]).doubleValue()).isCloseTo(60.00, offset(0.001));
        assertThat(both.get(1)[0]).isEqualTo("Gadgets");
        assertThat(((Number) both.get(1)[1]).doubleValue()).isCloseTo(200.00, offset(0.001));

        // (60): граница строгая -> "Books" (AVG ровно 60) исключена
        List<Object[]> boundary = invoke(productRepository,
                "categoryAvgPriceAbove",
                new Class<?>[] { BigDecimal.class },
                new BigDecimal("60"));
        assertThat(boundary).hasSize(1);
        assertThat(boundary.get(0)[0]).isEqualTo("Gadgets");

        // пустая категория никогда не возвращается (AVG NULL не проходит HAVING)
        assertThat(top).extracting(row -> ((Object[]) row)[0]).doesNotContain("Empty");
        assertThat(both).extracting(row -> ((Object[]) row)[0]).doesNotContain("Empty");
    }
}
