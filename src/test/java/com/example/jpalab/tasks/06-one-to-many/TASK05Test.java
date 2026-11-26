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
 * Тема 06, задание 05. Ловушка: добавление в коллекцию без обратной ссылки.
 *
 * Владелец FK — ManyToOne-сторона (product.category). mappedBy означает
 * «ключ живёт у тебя (у ManyToOne)»: коллекция может только навигировать
 * и НЕ умеет двигать чужой FK. Добавив товар в коллекцию B без
 * setCategory(B), мы не изменим ничего в БД: FK по-прежнему A.
 * Двунаправленную связь нужно поддерживать с ОБЕИХ сторон.
 *
 * «Ошибочное» добавление в коллекцию (без обратной ссылки) выполняет ВАШ
 * метод сервиса `void addProductToCollectionOnly(Long categoryId,
 * Long productId)` в `ProductLifecycleService` (загружает managed-категорию
 * и товар, добавляет в коллекцию, НЕ вызывает setCategory, flush) — до его
 * реализации тест красный.
 */
@DataJpaTest
@Import(ProductLifecycleService.class)
class TASK05Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductLifecycleService lifecycleService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void collectionAloneDoesNotMoveFk() {
        Category a = categoryRepository.saveAndFlush(Category.builder().name("RealOwner").build());
        Category b = categoryRepository.saveAndFlush(Category.builder().name("WantsToOwn").build());
        Product product = Product.builder()
                .name("Trapped").price(new BigDecimal("7.00")).stockQuantity(1).category(a).build();
        productRepository.saveAndFlush(product);
        em.clear();

        // managed-коллекция B; товар добавляет БЕЗ setCategory(b) ВАШ метод
        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        TaskTestSupport.invoke(lifecycleService, "addProductToCollectionOnly",
                new Class<?>[]{Long.class, Long.class}, b.getId(), product.getId());

        em.flush();

        // коллекция сама по себе не пишет FK: ни update, ни insert по товару
        assertThat(statistics.getEntityUpdateCount()).isZero();
        assertThat(statistics.getEntityInsertCount()).isZero();

        // FK НЕ изменился: товар по-прежнему в A
        Object fk = em.createNativeQuery(
                "SELECT category_id FROM products WHERE id = ?", Long.class)
                .setParameter(1, product.getId())
                .getSingleResult();
        assertThat(fk).isEqualTo(a.getId());
    }
}
