package com.example.jpalab.tasks.t10;

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
 * Тема 10, задание 02. Interface-based projection (open) через @Query.
 *
 * Существующий интерфейс ProductRepository.ProductSummary (name, price,
 * stockQuantity): JPQL выбирает скалярные поля, а Spring Data собирает
 * объекты-прокси интерфейса по соответствующим getter'ам — без конструктора
 * и без полных сущностей в памяти.
 */
@DataJpaTest
class TASK02Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void openInterfaceProjectionViaQuery() {
        Category c = categoryRepository.saveAndFlush(Category.builder().name("SummaryCat").build());
        productRepository.save(Product.builder()
                .name("A").price(new BigDecimal("10.00")).stockQuantity(0).category(c).build());
        productRepository.save(Product.builder()
                .name("B").price(new BigDecimal("20.00")).stockQuantity(5).category(c).build());
        productRepository.save(Product.builder()
                .name("C").price(new BigDecimal("30.00")).stockQuantity(5).category(c).build());

        List<ProductRepository.ProductSummary> rows = TaskTestSupport.invoke(
                productRepository, "summariesInStockAtLeast", new Class<?>[]{int.class}, 5);

        // stock >= 5: A (stock 0) не попадает, граница 5 включена
        assertThat(rows).hasSize(2);
        assertThat(rows)
                .extracting(ProductRepository.ProductSummary::getName)
                .containsExactly("B", "C");

        ProductRepository.ProductSummary b = rows.get(0);
        assertThat(b.getPrice()).isEqualByComparingTo("20.00");
        assertThat(b.getStockQuantity()).isEqualTo(5);
        assertThat(rows.get(1).getPrice()).isEqualByComparingTo("30.00");
        assertThat(rows.get(1).getStockQuantity()).isEqualTo(5);
    }
}
