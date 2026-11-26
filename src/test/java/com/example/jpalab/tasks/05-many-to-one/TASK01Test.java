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
 * Тема 05, задание 01. Первый ManyToOne: FK null и FK установлен.
 *
 * ManyToOne — «владелец» связи: колонку category_id он заполняет сам
 * при сохранении/обновлении. Реальное значение колонки проверяется
 * через native-запрос.
 *
 * Привязку товара к категории выполняет ВАШ метод сервиса
 * `Product attachCategoryToProduct(Long productId, String categoryName)`
 * в `ProductLifecycleService` — до его реализации тест красный.
 */
@DataJpaTest
@Import(ProductLifecycleService.class)
class TASK01Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductLifecycleService lifecycleService;

    @PersistenceContext
    private EntityManager em;

    private Object categoryColumn(Long productId) {
        return em.createNativeQuery(
                "SELECT category_id FROM products WHERE id = :id", Object.class)
                .setParameter("id", productId)
                .getSingleResult();
    }

    @Test
    void fkLifecycle() {
        // 1) товар БЕЗ категории: category_id в БД — NULL
        Product product = productRepository.saveAndFlush(Product.builder()
                .name("NoCat")
                .price(new BigDecimal("10.00"))
                .stockQuantity(1)
                .build());
        assertThat(categoryColumn(product.getId())).isNull();

        // 2) привязка к категории ВАШИМ методом сервиса
        //    (id товара при этом не меняется — обновляется существующая строка)
        categoryRepository.saveAndFlush(Category.builder().name("Cat").build());
        Product updated = TaskTestSupport.invoke(lifecycleService, "attachCategoryToProduct",
                new Class<?>[]{Long.class, String.class}, product.getId(), "Cat");
        assertThat(updated.getId()).isEqualTo(product.getId());

        // 3) category_id установлен на id категории
        assertThat(categoryColumn(product.getId())).isEqualTo(updated.getCategory().getId());
    }
}
