package com.example.jpalab.repository;

import com.example.jpalab.dto.ProductNamePrice;
import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 10. Проекции: interface-based (open) projection
 * и DTO/record через конструкторное выражение в JPQL.
 *
 * Проекция экономит память и трафик: в приложении оказываются
 * только нужные поля, а не полные сущности.
 */
@DataJpaTest
class ProjectionExampleTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private void saveProduct(String name, String price, int stock) {
        // уникальное имя категории: у Category.name стоит unique = true
        Category category = categoryRepository.save(Category.builder().name(name + " category").build());
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(stock)
                .category(category)
                .build());
    }

    private void prepareData() {
        saveProduct("Alpha", "100.00", 10);
        saveProduct("Beta", "200.00", 20);
        saveProduct("Gamma", "300.00", 30);
        saveProduct("Delta", "400.00", 40);
        saveProduct("Epsilon", "500.00", 50);
    }

    @Test
    void openInterfaceProjectionMapsGettersToEntityFields() {
        prepareData();

        // ProductSummary — интерфейс-маркер: Spring Data сам собирает
        // объекты по соответствующим полям Product (name, price, stockQuantity)
        List<ProductRepository.ProductSummary> summaries =
                productRepository.findSummariesByPriceGreaterThanOrderByNameAsc(new BigDecimal("200.00"));

        // GreaterThan — строго больше: Beta (200.00) не попадает в выборку
        assertThat(summaries).hasSize(3);
        assertThat(summaries)
                .extracting(ProductRepository.ProductSummary::getName)
                .containsExactly("Delta", "Epsilon", "Gamma");

        ProductRepository.ProductSummary delta = summaries.get(0);
        assertThat(delta.getName()).isEqualTo("Delta");
        assertThat(delta.getPrice()).isEqualByComparingTo("400.00");
        assertThat(delta.getStockQuantity()).isEqualTo(40);
    }

    @Test
    void recordProjectionViaConstructorExpression() {
        prepareData();

        // JPQL: SELECT new ProductNamePrice(p.name, p.price) —
        // конструктор record вызывается прямо в запросе
        List<ProductNamePrice> pairs = productRepository.namePricesAtLeast(new BigDecimal("400.00"));

        assertThat(pairs).containsExactly(
                new ProductNamePrice("Delta", new BigDecimal("400.00")),
                new ProductNamePrice("Epsilon", new BigDecimal("500.00")));

        // record: сравнение по значениям, удобный доступ к полям
        ProductNamePrice delta = pairs.get(0);
        assertThat(delta.name()).isEqualTo("Delta");
        assertThat(delta.price()).isEqualByComparingTo("400.00");
    }
}
