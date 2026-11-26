package com.example.jpalab.tasks.t06;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.service.ProductLifecycleService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 06, задание 07. Каскад удаления: сначала дети, потом родитель.
 *
 * cascade REMOVE распространяется на коллекцию. Порядок SQL-операций
 * выводится Hibernate из графа FK-зависимостей: delete по products
 * идёт ДО delete по categories. Обратный порядок упёрся бы в
 * FK-ограничение (products.category_id -> categories.id) и выбросил
 * DataIntegrityViolation — поэтому сам факт успешного flush является
 * доказательством правильного порядка (см. тему 5, задание 08).
 *
 * Удаляет категорию (каскадно — вместе с товарами) ВАШ метод сервиса
 * `void deleteCategoryWithCascade(Long categoryId)` в `ProductLifecycleService`
 * (загружает managed-категорию, em.remove, flush) — до его реализации
 * тест красный.
 */
@DataJpaTest
@Import(ProductLifecycleService.class)
class TASK07Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductLifecycleService lifecycleService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void cascadeRemoveOrder() {
        // категория с 3 товарами
        Category category = Category.builder().name("Parent").build();
        for (int i = 1; i <= 3; i++) {
            Product product = Product.builder()
                    .name("Child" + i)
                    .price(new BigDecimal(i + ".00"))
                    .stockQuantity(1)
                    .build();
            category.getProducts().add(product);
            product.setCategory(category);
        }
        categoryRepository.saveAndFlush(category);
        em.clear();

        // удаляем ТОЛЬКО категорию — ВАШИМ методом; если бы Hibernate
        // удалил её ДО детей, FK-ограничение выбросило бы
        // DataIntegrityViolation — flush прошёл, значит дети
        // (delete from products) ушли раньше родителя
        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();
        TaskTestSupport.invoke(lifecycleService, "deleteCategoryWithCascade",
                new Class<?>[]{Long.class}, category.getId());
        em.flush(); // успех = порядок «сначала дети» подтверждён FK-ом

        // и товары, и категория исчезли из БД
        Long productsLeft = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM products WHERE category_id = ?", Long.class)
                .setParameter(1, category.getId())
                .getSingleResult();
        assertThat(productsLeft).isZero();
        Long categoriesLeft = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM categories WHERE id = ?", Long.class)
                .setParameter(1, category.getId())
                .getSingleResult();
        assertThat(categoriesLeft).isZero();

        // 3 delete по товарам + 1 delete по категории
        assertThat(statistics.getEntityDeleteCount()).isEqualTo(4);
    }
}
