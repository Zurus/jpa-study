package com.example.jpalab.tasks.t05;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.service.ProductLifecycleService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 05, задание 05. Identity: одна инстанция категории на два товара.
 *
 * persistence context хранит инстанцию на (entity name, id): второй доступ
 * по той же сущности вернёт уже загруженный объект, а не новую копию
 * и не повторный SELECT.
 *
 * Загружает категорию товара ВАШ метод сервиса
 * `Category loadCategoryOfProduct(Long productId)` в `ProductLifecycleService`
 * (находит товар по id и возвращает его категорию) — до его реализации
 * тест красный.
 */
@DataJpaTest
@Import(ProductLifecycleService.class)
class TASK05Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductLifecycleService lifecycleService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void sameCategoryInstance() {
        // 2 товара, 1 категория
        Category category = categoryRepository.saveAndFlush(
                Category.builder().name("Shared").build());
        Product p1 = productRepository.saveAndFlush(Product.builder()
                .name("P1").price(new BigDecimal("10.00")).stockQuantity(1)
                .category(category).build());
        Product p2 = productRepository.saveAndFlush(Product.builder()
                .name("P2").price(new BigDecimal("20.00")).stockQuantity(1)
                .category(category).build());

        // свежий persistence context: категории будут прокси
        em.clear();
        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        Product loaded1 = productRepository.findById(p1.getId()).orElseThrow();
        Product loaded2 = productRepository.findById(p2.getId()).orElseThrow();

        // до первого доступа — прокси
        assertThat(Hibernate.isInitialized(loaded1.getCategory())).isFalse();
        assertThat(Hibernate.isInitialized(loaded2.getCategory())).isFalse();

        // инициализация через ПЕРВЫЙ товар (ваш метод сервиса)
        Category c1 = TaskTestSupport.invoke(lifecycleService, "loadCategoryOfProduct",
                new Class<?>[]{Long.class}, loaded1.getId());
        c1.getName();

        // у ВТОРОГО — тот же объект (identity-карта L1-кэша)
        Category c2 = TaskTestSupport.invoke(lifecycleService, "loadCategoryOfProduct",
                new Class<?>[]{Long.class}, loaded2.getId());
        assertThat(c2).isSameAs(c1);

        // 2 SELECT на товары + ровно 1 SELECT на категорию
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);
    }
}
