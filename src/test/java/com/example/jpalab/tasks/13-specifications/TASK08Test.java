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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 13, задание 08. Предикат по свойству-пути через ассоциацию
 * (category.name, case-insensitive contains) + комбинация с Pageable:
 * Page через JpaSpecificationExecutor (count + данные). Метод-артефакт:
 * ProductSpecifications.byCategoryNameContaining(String).
 */
@DataJpaTest
class TASK08Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void categoryPathPredicateWithPage() {
        Category shelf = categoryRepository.save(Category.builder().name("Alpha Shelf").build());
        Category rack = categoryRepository.save(Category.builder().name("Beta Rack").build());
        productRepository.save(Product.builder().name("Shelf 1").price(new BigDecimal("10.00"))
                .stockQuantity(1).category(shelf).build());
        productRepository.save(Product.builder().name("Shelf 2").price(new BigDecimal("30.00"))
                .stockQuantity(2).category(shelf).build());
        productRepository.save(Product.builder().name("Shelf 3").price(new BigDecimal("20.00"))
                .stockQuantity(3).category(shelf).build());
        productRepository.save(Product.builder().name("Rack 1").price(new BigDecimal("5.00"))
                .stockQuantity(4).category(rack).build());

        Object specs = TaskTestSupport.newInstance("com.example.jpalab.repository.ProductSpecifications");

        Specification<Product> shelfSpec =
                TaskTestSupport.invoke(specs, "byCategoryNameContaining", new Class<?>[]{String.class}, "SHELF");

        // count по спецификации — отдельный метод JpaSpecificationExecutor
        assertThat(productRepository.count(shelfSpec)).isEqualTo(3);

        // Specification + Pageable: страница size=2, сортировка по цене
        Page<Product> page = productRepository.findAll(shelfSpec,
                PageRequest.of(0, 2, Sort.by("price")));
        List<String> pageNames = page.getContent().stream().map(Product::getName).toList();
        assertThat(pageNames).containsExactly("Shelf 1", "Shelf 3");
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.hasNext()).isTrue();

        // вторая категория
        Specification<Product> rackSpec =
                TaskTestSupport.invoke(specs, "byCategoryNameContaining", new Class<?>[]{String.class}, "RACK");
        assertThat(productRepository.count(rackSpec)).isEqualTo(1);
        Page<Product> rackPage = productRepository.findAll(rackSpec, PageRequest.of(0, 10));
        assertThat(rackPage.getContent()).extracting(Product::getName).containsExactly("Rack 1");
    }
}
