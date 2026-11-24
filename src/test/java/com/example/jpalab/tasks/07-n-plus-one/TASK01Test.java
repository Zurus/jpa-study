package com.example.jpalab.tasks.t07;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
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
 * Тема 07, задание 01. Измерить N+1 на @ManyToOne (LAZY).
 *
 * Схема «1+N»: одна SELECT по таблице сущностей (products) + по одной
 * SELECT на каждую LAZY-ассоциацию (category), которую мы «дёргаем» в
 * цикле. Без em.clear() после создания данных N+1 не видно —
 * persistence context уже «знает» все объекты.
 */
@DataJpaTest
class TASK01Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void measureNPlus1OnManyToOne() {
        // 5 товаров, 5 РАЗНЫХ категорий
        for (int i = 1; i <= 5; i++) {
            Category category = categoryRepository.save(
                    Category.builder().name("N1 C" + i).build());
            Product product = Product.builder()
                    .name("N1 P" + i)
                    .price(BigDecimal.valueOf(i * 10L, 2))
                    .stockQuantity(1)
                    .category(category)
                    .build();
            productRepository.save(product);
        }
        em.flush();
        em.clear();

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        // 1 SELECT по products
        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(5);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        // категории ещё НЕ инициализированы (LAZY-прокси)
        for (Product product : products) {
            assertThat(Hibernate.isInitialized(product.getCategory())).isFalse();
        }

        // цикл доступа к категориям: +5 SELECT (по одному на товар)
        for (Product product : products) {
            product.getCategory().getName();
        }
        // итого 1 + N = 6
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(6);
    }
}
