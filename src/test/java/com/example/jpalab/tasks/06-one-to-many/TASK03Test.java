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
 * Тема 06, задание 03. orphanRemoval: удаление из коллекции.
 *
 * orphanRemoval = true превращает «выпадение из коллекции» в удаление
 * сущности (неявно влечёт cascade REMOVE): достаточно remove managed-
 * элемента из коллекции managed-родителя, em.remove не понадобился —
 * в БД уходит delete from products where id = ?.
 *
 * Удаление выполняет ВАШ метод сервиса
 * `void removeProductFromCategory(Long categoryId, String productName)` в
 * `ProductLifecycleService` (загружает managed-категорию и убирает товар
 * ТОЛЬКО из её коллекции — без em.remove) — до его реализации тест красный.
 */
@DataJpaTest
@Import(ProductLifecycleService.class)
class TASK03Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductLifecycleService lifecycleService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void orphanRemoval() {
        // категория с 2 товарами
        Category category = Category.builder().name("OrphanCat").build();
        Product keep = Product.builder().name("Keep").price(new BigDecimal("1.00")).stockQuantity(1).build();
        Product removeMe = Product.builder().name("RemoveMe").price(new BigDecimal("2.00")).stockQuantity(1).build();
        category.getProducts().add(keep);
        keep.setCategory(category);
        category.getProducts().add(removeMe);
        removeMe.setCategory(category);
        categoryRepository.saveAndFlush(category);
        em.clear();

        // managed-родитель; товар убирает из коллекции ВАШ метод сервиса
        // (em.remove НЕ вызывается ни в тесте, ни в сервисе)
        Category managed = categoryRepository.findById(category.getId()).orElseThrow();
        assertThat(managed.getProducts()).hasSize(2);

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        TaskTestSupport.invoke(lifecycleService, "removeProductFromCategory",
                new Class<?>[]{Long.class, String.class}, category.getId(), "RemoveMe");

        em.flush();

        // ровно один delete по товару
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
        assertThat(statistics.getEntityDeleteCount()).isEqualTo(1);

        // строка удалённого товара исчезла, второй на месте
        Long inCategory = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM products WHERE category_id = ?", Long.class)
                .setParameter(1, category.getId())
                .getSingleResult();
        assertThat(inCategory).isEqualTo(1L);
        Long byName = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM products WHERE name = ?", Long.class)
                .setParameter(1, "RemoveMe")
                .getSingleResult();
        assertThat(byName).isZero();
    }
}
