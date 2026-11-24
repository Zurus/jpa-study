package com.example.jpalab.repository;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.Attribute;
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
 * Тема 5. @ManyToOne + @JoinColumn, LAZY vs EAGER.
 *
 * В этом проекте Product.category объявлена как LAZY (рекомендуемый дефолт для @ManyToOne).
 * "Eager"-поведение показываем через fetch join — стандартный способ загрузить
 * связанную сущность вместе с родителем одним запросом.
 */
@DataJpaTest
class ManyToOneExampleTest {

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

    @Test
    void manyToOneMappingUsesJoinColumn() {
        // Метамодель: Product.category — ассоциация MANY_TO_ONE на Category
        var attribute = em.getMetamodel().entity(Product.class).getAttribute("category");
        assertThat(attribute.getName()).isEqualTo("category");
        assertThat(attribute.getPersistentAttributeType())
                .isEqualTo(Attribute.PersistentAttributeType.MANY_TO_ONE);
        assertThat(attribute.getJavaType()).isEqualTo(Category.class);
        assertThat(attribute.isAssociation()).isTrue();
        assertThat(attribute.isCollection()).isFalse();

        // @JoinColumn(name = "category_id"): FK в products указывает на categories.id
        Category category = categoryRepository.save(Category.builder().name("Gadgets").build());
        Product saved = productRepository.save(Product.builder()
                .name("Phone A").price(new BigDecimal("500.00")).stockQuantity(1)
                .category(category).build());
        em.flush();

        Long fk = (Long) em.createNativeQuery(
                        "SELECT category_id FROM products WHERE id = :pid", Long.class)
                .setParameter("pid", saved.getId())
                .getSingleResult();
        assertThat(fk).isEqualTo(category.getId());
    }

    @Test
    void lazyCategoryIsLoadedOnlyWhenAccessed() {
        Category category = categoryRepository.save(Category.builder().name("Gadgets").build());
        Product saved = productRepository.save(Product.builder()
                .name("Phone A").price(new BigDecimal("500.00")).stockQuantity(1)
                .category(category).build());
        em.flush();
        em.clear(); // "свежая" сессия, как будто данные пришли из БД

        statistics().clear();
        Product product = productRepository.findById(saved.getId()).orElseThrow();

        // LAZY: на выборку товара потрачен ровно 1 SQL, категория не запрашивалась
        assertThat(preparedStatementCount()).isEqualTo(1);
        Category proxy = product.getCategory();
        assertThat(proxy).as("lazy-ассоциация возвращает прокси").isNotNull();
        assertThat(Hibernate.isInitialized(proxy)).isFalse();

        statistics().clear();
        String name = proxy.getName(); // только обращение к свойству запускает дозагрузку

        assertThat(name).isEqualTo("Gadgets");
        assertThat(preparedStatementCount()).isEqualTo(1); // +1 SELECT к таблице categories
        assertThat(Hibernate.isInitialized(proxy)).isTrue();
    }

    @Test
    void fetchJoinLoadsCategoryEagerlyInSingleQuery() {
        Category gadgets = categoryRepository.save(Category.builder().name("Gadgets").build());
        Category audio = categoryRepository.save(Category.builder().name("Audio").build());
        productRepository.save(Product.builder()
                .name("Phone A").price(new BigDecimal("500.00")).stockQuantity(1)
                .category(gadgets).build());
        productRepository.save(Product.builder()
                .name("Headphones B").price(new BigDecimal("200.00")).stockQuantity(2)
                .category(audio).build());
        em.flush();
        em.clear();

        statistics().clear();
        List<Product> products = productRepository.findAllWithCategoryFetched();

        // EAGER-загрузка: один SELECT ... JOIN, и у каждого товара категория уже инициализирована
        assertThat(preparedStatementCount()).isEqualTo(1);
        assertThat(products)
                .hasSize(2)
                .allSatisfy(p -> assertThat(Hibernate.isInitialized(p.getCategory())).isTrue());
    }
}
