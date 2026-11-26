package com.example.jpalab.tasks.t03;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
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
 * Тема 03, задание 09. Результат native-запроса — managed-сущность.
 *
 * Native-запрос на сущность не «обходит» persistence context — он питает его.
 * Запрос «товар по id» выполняется ВАШЕМ native-методом репозитория
 * `List<Product> findNativeById(Long id)` (native SELECT * по id) — до его
 * реализации тест красный. Свойства, которые доказывает тест:
 * - результат native-SELECT и em.find — одна и та же инстанция;
 * - em.contains для результата native-запроса — true;
 * - изменение такой сущности и flush порождают UPDATE.
 */
@DataJpaTest
class TASK09Test {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    private List<Product> nativeById(Long id) {
        return TaskTestSupport.invoke(productRepository, "findNativeById",
                new Class<?>[]{Long.class}, id);
    }

    @Test
    void nativeResultsAreManaged() {
        // 1. сохраняем товар (цена 100)
        Product saved = productRepository.saveAndFlush(Product.builder()
                .name("Managed")
                .price(new BigDecimal("100.00"))
                .stockQuantity(1)
                .build());

        // 2. native: SELECT * FROM products WHERE id = :id
        Product fromNative = nativeById(saved.getId()).get(0);

        // 3. em.find(Product.class, id)
        Product fromFind = em.find(Product.class, saved.getId());

        // одна и та же инстанция
        assertThat(fromNative).isSameAs(fromFind);
        // native-результат проходит через тот же L1-кэш
        assertThat(em.contains(fromNative)).isTrue();

        // изменение цены и flush -> UPDATE, повторный native-SELECT видит новую цену
        fromNative.setPrice(new BigDecimal("250.00"));
        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();
        em.flush();
        assertThat(statistics.getEntityUpdateCount()).isGreaterThanOrEqualTo(1);

        Product reloaded = nativeById(saved.getId()).get(0);
        assertThat(reloaded).isSameAs(fromNative);
        assertThat(reloaded.getPrice()).isEqualByComparingTo("250.00");
    }
}
