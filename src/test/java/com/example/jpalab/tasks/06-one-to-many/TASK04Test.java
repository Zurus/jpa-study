package com.example.jpalab.tasks.t06;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
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
 * Тема 06, задание 04. Перенос товара между категориями.
 *
 * «Перенос» — это смена FK, а не пересоздание строки: коллекция — лишь
 * навигация, реальное изменение — колонка category_id на стороне товара
 * (владелец FK — ManyToOne-сторона). В SQL-логе ровно один
 * update products set ... category_id = ?, без delete/insert.
 *
 * Перенос выполняет ВАШ метод сервиса
 * `void moveProductToCategory(Long productId, Long targetCategoryId)` в
 * `ProductLifecycleService` (загружает managed-категории и товар, убирает
 * из «старой» коллекции, добавляет в «новую», обновляет обратную ссылку,
 * flush) — до его реализации тест красный.
 */
@DataJpaTest
@Import(ProductLifecycleService.class)
class TASK04Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductLifecycleService lifecycleService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void moveBetweenCategories() {
        Category a = categoryRepository.saveAndFlush(Category.builder().name("From").build());
        Category b = categoryRepository.saveAndFlush(Category.builder().name("To").build());
        Product product = Product.builder()
                .name("Mover").price(new BigDecimal("9.00")).stockQuantity(1).category(a).build();
        productRepository.saveAndFlush(product);
        em.clear();

        // перенос — ВАШИМ методом: убрать из A, добавить в B,
        // обновить обратную ссылку
        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        TaskTestSupport.invoke(lifecycleService, "moveProductToCategory",
                new Class<?>[]{Long.class, Long.class}, product.getId(), b.getId());

        em.flush();

        // ровно один UPDATE FK; НЕТ delete/insert по товару
        assertThat(statistics.getEntityUpdateCount()).isEqualTo(1);
        assertThat(statistics.getEntityInsertCount()).isZero();
        assertThat(statistics.getEntityDeleteCount()).isZero();

        // товар всё тот же (id не изменился), строка одна
        Object fk = em.createNativeQuery(
                "SELECT category_id FROM products WHERE id = ?", Long.class)
                .setParameter(1, product.getId())
                .getSingleResult();
        assertThat(fk).isEqualTo(b.getId());
        Long total = (Long) em.createNativeQuery("SELECT COUNT(*) FROM products", Long.class)
                .getSingleResult();
        assertThat(total).isEqualTo(1L);
    }
}
