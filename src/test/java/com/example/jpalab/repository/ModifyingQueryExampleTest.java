package com.example.jpalab.repository;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 9. @Modifying: bulk UPDATE/DELETE через JPQL.
 *
 * Bulk-запросы обновляют БД напрямую, минуя persistence context,
 * поэтому флаг clearAutomatically (и flushAutomatically) важен:
 * без них следующие SELECT могут вернуть устаревшие сущности из PC.
 */
@DataJpaTest
class ModifyingQueryExampleTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    private Product saveProduct(String name, String price, int stock) {
        // уникальное имя категории: у Category.name стоит unique = true
        Category category = categoryRepository.save(Category.builder().name(name + " category").build());
        return productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(stock)
                .category(category)
                .build());
    }

    @Test
    void modifyingUpdateReturnsAffectedRowsAndDbReflectsChanges() {
        saveProduct("Cheap", "100.00", 1);
        saveProduct("Mid", "500.00", 2);
        saveProduct("Expensive", "900.00", 3);

        // UPDATE Product SET stock = 42 WHERE price >= 500 -> затронуто 2 строки
        int updated = productRepository.updateStockForExpensive(42, new BigDecimal("500.00"));

        assertThat(updated).isEqualTo(2);
        // clearAutomatically = true: PC очищен, SELECT видит свежее состояние
        List<Product> all = productRepository.findAll();
        assertThat(all)
                .filteredOn(p -> "Cheap".equals(p.getName()))
                .first()
                .extracting(Product::getStockQuantity)
                .isEqualTo(1);
        assertThat(all)
                .filteredOn(p -> "Mid".equals(p.getName()) || "Expensive".equals(p.getName()))
                .allSatisfy(p -> assertThat(p.getStockQuantity()).isEqualTo(42));
    }

    @Test
    void modifyingDeleteReturnsDeletedRowsAndDbReflectsChanges() {
        saveProduct("OutOfStock A", "100.00", 0);
        saveProduct("OutOfStock B", "200.00", 0);
        saveProduct("InStock", "300.00", 5);

        int deleted = productRepository.deleteOutOfStock();

        assertThat(deleted).isEqualTo(2);
        List<Product> remaining = productRepository.findAll();
        assertThat(remaining)
                .extracting(Product::getName)
                .containsExactly("InStock");
    }

    @Test
    void withoutClearAutomaticallySelectSeesStalePersistenceContext() {
        // Сущности сохранены и остались в persistence context (та же транзакция)
        Product cheap = saveProduct("Cheap", "100.00", 1);
        saveProduct("Mid", "200.00", 2);

        int updated = productRepository.bumpAllPricesBy(new BigDecimal("100.00"));
        assertThat(updated).isEqualTo(2);

        // Без clearAutomatically: у Hibernate в PC все еще старые экземпляры,
        // и SELECT их вернет (первый уровень кэша), а не данные из БД
        Product stale = productRepository.findById(cheap.getId()).orElseThrow();
        assertThat(stale.getPrice()).isEqualByComparingTo("100.00"); // устаревшее значение

        // После em.clear() SELECT идет в БД — цена обновлена
        em.clear();
        Product fresh = productRepository.findById(cheap.getId()).orElseThrow();
        assertThat(fresh.getPrice()).isEqualByComparingTo("200.00");
    }
}
