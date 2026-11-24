package com.example.jpalab.tasks.t07;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 07, задание 02. Измерить N+1 на @OneToMany (LAZY-коллекции).
 *
 * N+1 для коллекций — это N загрузок КОЛЛЕКЦИЙ (по одной на родителя),
 * а не N × размер коллекции: у первой категории 3 товара, у четвёртой 1,
 * а запросов одинаково — по одному на категорию.
 */
@DataJpaTest
class TASK02Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void measureNPlus1OnCollections() {
        // 4 категории с разным числом товаров: 3, 2, 1, 1
        int[] sizes = {3, 2, 1, 1};
        for (int i = 0; i < sizes.length; i++) {
            Category category = categoryRepository.save(
                    Category.builder().name("N2 C" + i).build());
            for (int j = 0; j < sizes[i]; j++) {
                Product product = Product.builder()
                        .name("N2 C" + i + " P" + j)
                        .price(BigDecimal.valueOf(i * 10L + j, 2))
                        .stockQuantity(1)
                        .category(category)
                        .build();
                productRepository.save(product);
            }
        }
        em.flush();
        em.clear();

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        // 1 SELECT по categories
        List<Category> categories = categoryRepository.findAll();
        assertThat(categories).hasSize(4);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        // цикл: инициализация LAZY-коллекции — 1 SELECT на категорию,
        // независимо от размера коллекции
        for (Category category : categories) {
            category.getProducts().size();
        }
        // итого 1 + 4 = 5
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(5);
    }
}
