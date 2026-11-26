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
 * Тема 05, задание 03. Отвязка: category = null.
 *
 * nullable-колонка FK позволяет null; Hibernate различает «поле не трогай»
 * и «поставь null» по ссылке на объект: setCategory(null) → UPDATE с NULL-
 * параметром. Повторная загрузка сущности проходит без ошибок.
 *
 * Отвязку выполняет ВАШ метод сервиса
 * `Product detachProductFromCategory(Long productId)` в
 * `ProductLifecycleService` — до его реализации тест красный.
 */
@DataJpaTest
@Import(ProductLifecycleService.class)
class TASK03Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductLifecycleService lifecycleService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void detachFromCategory() {
        Category a = categoryRepository.saveAndFlush(
                Category.builder().name("A").build());
        Product product = productRepository.saveAndFlush(Product.builder()
                .name("Unlink")
                .price(new BigDecimal("10.00"))
                .stockQuantity(1)
                .category(a)
                .build());

        // отвязка ВАШИМ методом + flush внутри сервиса: без NPE и без падения
        TaskTestSupport.invoke(lifecycleService, "detachProductFromCategory",
                new Class<?>[]{Long.class}, product.getId());

        // FK в БД стал NULL
        Object fk = em.createNativeQuery(
                "SELECT category_id FROM products WHERE id = :id", Object.class)
                .setParameter("id", product.getId())
                .getSingleResult();
        assertThat(fk).isNull();

        // повторная загрузка (новый persistence context) — без ошибки,
        // getCategory() — null (не прокси)
        em.clear();
        Product reloaded = productRepository.findById(product.getId()).orElseThrow();
        assertThat(reloaded.getCategory()).isNull();
    }
}
