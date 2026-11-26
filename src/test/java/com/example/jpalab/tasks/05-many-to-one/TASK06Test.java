package com.example.jpalab.tasks.t05;

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
 * Тема 05, задание 06. Dirty checking: FK и поле в одном UPDATE.
 *
 * Dirty checking сравнивает снапшот (snapshot) с текущим состоянием
 * и строит ОДИН UPDATE на сущность, а не по UPDATE на поле:
 * price + category_id (и version) — в одном update products.
 *
 * Оба изменения выполняет ВАШ метод сервиса
 * `Product updatePriceAndCategory(Long productId, BigDecimal newPrice,
 * String newCategoryName)` в `ProductLifecycleService` — до его реализации
 * тест красный.
 */
@DataJpaTest
@Import(ProductLifecycleService.class)
class TASK06Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductLifecycleService lifecycleService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void singleUpdateTwoColumns() {
        Category a = categoryRepository.saveAndFlush(
                Category.builder().name("A").build());
        Category b = categoryRepository.saveAndFlush(
                Category.builder().name("B").build());

        Product product = productRepository.saveAndFlush(Product.builder()
                .name("Dirty")
                .price(new BigDecimal("100.00"))
                .stockQuantity(1)
                .category(a)
                .build());

        // «испортить» сущность сразу в двух местах — ВАШИМ методом
        // (setPrice + setCategory внутри одной транзакции)
        TaskTestSupport.invoke(lifecycleService, "updatePriceAndCategory",
                new Class<?>[]{Long.class, BigDecimal.class, String.class},
                product.getId(), new BigDecimal("150.00"), "B");

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        // один flush (вызванный сервисом)
        em.flush();

        // ровно один UPDATE сущности (в нём — price, category_id и version;
        // список колонок виден в SQL-логе)
        assertThat(statistics.getEntityUpdateCount()).isEqualTo(1);

        // значения в БД: новая цена и новый FK
        Object[] row = (Object[]) em.createNativeQuery(
                "SELECT price, category_id FROM products WHERE id = :id", Object[].class)
                .setParameter("id", product.getId())
                .getSingleResult();
        assertThat((BigDecimal) row[0]).isEqualByComparingTo("150.00");
        assertThat(row[1]).isEqualTo(b.getId());
    }
}
