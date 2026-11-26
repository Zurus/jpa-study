package com.example.jpalab.tasks.t13;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import org.springframework.data.jpa.domain.Specification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 13, задание 01. Первый Specification: точное совпадение по имени.
 *
 * Студент создаёт класс com.example.jpalab.repository.ProductSpecifications
 * со статическим методом byName(String), возвращающим Specification&lt;Product&gt;
 * (предикат eq на name). Тест находит класс и метод рефлексивно через
 * TaskTestSupport и проверяет результат productRepository.findAll(spec)
 * (JpaSpecificationExecutor уже добавлен в ProductRepository).
 * До реализации — красный (класс/метод отсутствуют).
 */
@DataJpaTest
class TASK01Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void byNameFindsExactMatch() {
        Category category = categoryRepository.save(Category.builder().name("Misc").build());
        productRepository.save(Product.builder().name("Widget A").price(new BigDecimal("10.00"))
                .stockQuantity(1).category(category).build());
        productRepository.save(Product.builder().name("Widget B").price(new BigDecimal("20.00"))
                .stockQuantity(2).category(category).build());
        productRepository.save(Product.builder().name("Gadget").price(new BigDecimal("30.00"))
                .stockQuantity(0).category(category).build());

        Object specs = TaskTestSupport.newInstance("com.example.jpalab.repository.ProductSpecifications");

        Specification<Product> spec =
                TaskTestSupport.invoke(specs, "byName", new Class<?>[]{String.class}, "Widget B");
        List<Product> found = productRepository.findAll(spec);
        assertThat(found).extracting(Product::getName).containsExactly("Widget B");

        // «eq» — точное совпадение: "Widget" без уточнения ничего не находит
        Specification<Product> partial =
                TaskTestSupport.invoke(specs, "byName", new Class<?>[]{String.class}, "Widget");
        assertThat(productRepository.findAll(partial)).isEmpty();
    }
}
