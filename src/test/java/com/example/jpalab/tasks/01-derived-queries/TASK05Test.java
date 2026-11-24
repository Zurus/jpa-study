package com.example.jpalab.tasks.t01;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 01, задание 05. Свойство-путь через ассоциацию + First.
 *
 * Целевой метод (добавить в ProductRepository, без @Query):
 * <pre>{@code
 * Optional<Product> findFirstByCategoryName(String categoryName);
 * }</pre>
 * Метод принимает строку category.name (а не объект Category) и возвращает
 * первый (любой, но ровно один) товар категории.
 */
@DataJpaTest
class TASK05Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final Map<String, Category> categories = new HashMap<>();

    private void saveIn(String categoryName, String productName, String price) {
        Category category = categories.computeIfAbsent(categoryName, name ->
                categoryRepository.saveAndFlush(Category.builder().name(name).build()));
        productRepository.save(Product.builder()
                .name(productName)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .category(category)
                .build());
    }

    @Test
    void findFirstByCategoryNameReturnsSingleProduct() {
        // категория "Gadgets" с 2 товарами + другая категория-помеха
        saveIn("Gadgets", "Gadget A", "10.00");
        saveIn("Gadgets", "Gadget B", "20.00");
        saveIn("Other", "Other One", "5.00");

        // из категории с 2 товарами возвращается ровно 1
        Optional<Product> first = invoke(productRepository,
                "findFirstByCategoryName", new Class<?>[]{String.class}, "Gadgets");
        assertThat(first).isPresent();
        assertThat(first.get().getName()).isIn("Gadget A", "Gadget B");

        // товара из другой категории быть не должно
        assertThat(first.get().getName()).isNotEqualTo("Other One");

        // несуществующее имя категории -> пустой Optional без ошибки
        Optional<Product> missing = invoke(productRepository,
                "findFirstByCategoryName", new Class<?>[]{String.class}, "Missing Category");
        assertThat(missing).isEmpty();
    }
}
