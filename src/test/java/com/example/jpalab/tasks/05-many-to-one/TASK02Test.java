package com.example.jpalab.tasks.t05;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.service.ProductLifecycleService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 05, задание 02. Смена FK: привязка товара к другой категории.
 *
 * Изменение навигационного поля managed-сущности — это «грязь», которую
 * dirty checking переведёт в UPDATE: id товара не меняется, строка не
 * пересоздаётся (нет delete/insert), обновляется только category_id.
 *
 * Смену категории выполняет ВАШ метод сервиса
 * `Product reassignProductCategory(Long productId, String newCategoryName)`
 * в `ProductLifecycleService` — до его реализации тест красный.
 */
@DataJpaTest
@Import(ProductLifecycleService.class)
class TASK02Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductLifecycleService lifecycleService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void reassignCategory() {
        Category a = categoryRepository.saveAndFlush(
                Category.builder().name("A").build());
        Category b = categoryRepository.saveAndFlush(
                Category.builder().name("B").build());

        Product product = productRepository.saveAndFlush(Product.builder()
                .name("Switch")
                .price(new BigDecimal("10.00"))
                .stockQuantity(1)
                .category(a)
                .build());
        Long originalId = product.getId();

        // один вызов ВАШЕГО метода + flush внутри сервиса
        Product updated = TaskTestSupport.invoke(lifecycleService, "reassignProductCategory",
                new Class<?>[]{Long.class, String.class}, product.getId(), "B");

        // id товара не изменился (строка не пересоздавалась)
        assertThat(updated.getId()).isEqualTo(originalId);

        // в БД обновился внешний ключ
        Object fk = em.createNativeQuery(
                "SELECT category_id FROM products WHERE id = :id", Object.class)
                .setParameter("id", product.getId())
                .getSingleResult();
        assertThat(fk).isEqualTo(b.getId());

        // в памяти — категория B
        assertThat(updated.getCategory().getName()).isEqualTo("B");
    }
}
