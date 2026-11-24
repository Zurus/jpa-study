package com.example.jpalab.repository;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 4. GROUP BY и HAVING через JPQL.
 */
@DataJpaTest
class GroupByExampleTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category saveCategory(String name) {
        return categoryRepository.save(Category.builder().name(name).build());
    }

    private void addProduct(Category category, String name, String price) {
        Product product = Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .category(category)
                .build();
        category.getProducts().add(product);
        productRepository.save(product);
    }

    /** 3 категории: Smartphones (3 товара, сумма 600), Laptops (1 товар, 500), Accessories (пусто). */
    private void prepareData() {
        Category smartphones = saveCategory("Smartphones");
        addProduct(smartphones, "S1", "100.00");
        addProduct(smartphones, "S2", "200.00");
        addProduct(smartphones, "S3", "300.00");

        Category laptops = saveCategory("Laptops");
        addProduct(laptops, "L1", "500.00");

        saveCategory("Accessories");
    }

    @Test
    void groupByAggregatesCountAndSumPerCategory() {
        prepareData();

        // GROUP BY c.name: одна строка на категорию; LEFT JOIN сохраняет пустую группу
        List<Object[]> stats = categoryRepository.productStatsByCategory();

        assertThat(stats).hasSize(3);

        Object[] accessories = stats.get(0);
        assertThat(accessories[0]).isEqualTo("Accessories");
        assertThat((Long) accessories[1]).isZero();
        assertThat(accessories[2]).as("SUM по пустой группе = null").isNull();

        Object[] laptops = stats.get(1);
        assertThat(laptops[0]).isEqualTo("Laptops");
        assertThat((Long) laptops[1]).isEqualTo(1L);
        assertThat((BigDecimal) laptops[2]).isEqualByComparingTo("500.00");

        Object[] smartphones = stats.get(2);
        assertThat(smartphones[0]).isEqualTo("Smartphones");
        assertThat((Long) smartphones[1]).isEqualTo(3L);
        assertThat((BigDecimal) smartphones[2]).isEqualByComparingTo("600.00");
    }

    @Test
    void havingFiltersGroupsByAggregate() {
        prepareData();

        // HAVING COUNT(p.id) >= 2 остается только Smartphones
        List<String> names = categoryRepository.categoryNamesWithAtLeastProducts(2);

        assertThat(names).containsExactly("Smartphones");

        assertThat(categoryRepository.categoryNamesWithAtLeastProducts(1))
                .containsExactly("Laptops", "Smartphones");
    }
}
