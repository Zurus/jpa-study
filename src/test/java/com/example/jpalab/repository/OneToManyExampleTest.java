package com.example.jpalab.repository;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 6. @OneToMany + mappedBy, cascade = ALL, orphanRemoval, LAZY-коллекция.
 */
@DataJpaTest
class OneToManyExampleTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    private long preparedStatementCount() {
        return em.unwrap(Session.class).getSessionFactory().getStatistics().getPrepareStatementCount();
    }

    private Statistics statistics() {
        Statistics stats = em.unwrap(Session.class).getSessionFactory().getStatistics();
        assertThat(stats).as("hibernate.generate_statistics должен быть true").isNotNull();
        return stats;
    }

    /** Категория с двумя товарами, сохраненная САМОЙ собой (каскад проверяется этим). */
    private Category saveCategoryWithTwoProducts() {
        Category category = Category.builder().name("Gadgets").build();
        Product phone = Product.builder().name("Phone A").price(new BigDecimal("500.00"))
                .stockQuantity(1).category(category).build();
        Product tablet = Product.builder().name("Tablet C").price(new BigDecimal("900.00"))
                .stockQuantity(2).category(category).build();
        category.getProducts().add(phone);
        category.getProducts().add(tablet);
        return categoryRepository.save(category); // сохраняем только категорию
    }

    @Test
    void cascadeAllPersistsProductsThroughCategory() {
        Category saved = saveCategoryWithTwoProducts();
        em.flush();
        em.clear();

        // В products репо мы товары НЕ сохраняли — их записал cascade = ALL
        List<Product> products = productRepository.findByCategory(saved);
        assertThat(products)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Phone A", "Tablet C");
    }

    @Test
    void orphanRemovalDeletesProductRemovedFromCollection() {
        Category saved = saveCategoryWithTwoProducts();
        em.clear();

        Category managed = categoryRepository.findById(saved.getId()).orElseThrow();
        assertThat(productRepository.findByCategory(managed)).hasSize(2);

        // Убираем товар из коллекции: orphanRemoval = true -> строка удалится из БД
        Product orphaned = managed.getProducts().stream()
                .filter(p -> "Phone A".equals(p.getName()))
                .findFirst()
                .orElseThrow();
        managed.getProducts().remove(orphaned);
        em.flush();
        em.clear();

        List<Product> remaining = productRepository.findByCategory(saved);
        assertThat(remaining)
                .extracting(Product::getName)
                .containsExactly("Tablet C");
    }

    @Test
    void lazyCollectionIsLoadedOnlyOnAccess() {
        Category saved = saveCategoryWithTwoProducts();
        em.flush();
        em.clear();

        statistics().clear();
        Category category = categoryRepository.findById(saved.getId()).orElseThrow();

        // LAZY-коллекция: сама категория читается одним SELECT,
        // содержимое products (PersistentBag) еще не инициализировано
        assertThat(preparedStatementCount()).isEqualTo(1);
        assertThat(Hibernate.isInitialized(category.getProducts())).isFalse();

        statistics().clear();
        int size = category.getProducts().size(); // обращение догружает коллекцию

        assertThat(size).isEqualTo(2);
        assertThat(preparedStatementCount()).isEqualTo(1); // +1 SELECT к таблице products
        assertThat(Hibernate.isInitialized(category.getProducts())).isTrue();
    }
}
