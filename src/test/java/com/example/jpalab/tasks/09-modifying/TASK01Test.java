package com.example.jpalab.tasks.t09;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

/**
 * Тема 09, задание 01. Первый bulk UPDATE с @Modifying.
 *
 * Bulk UPDATE в JPQL меняет строки в БД напрямую, минуя persistence context:
 * один SQL-вызов update products ... where ..., без per-entity SELECT/UPDATE.
 * Возвращаемое значение @Modifying-метода — количество затронутых строк.
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
    void firstBulkUpdate() {
        Category a = categoryRepository.saveAndFlush(Category.builder().name("A").build());
        Category b = categoryRepository.saveAndFlush(Category.builder().name("B").build());
        productRepository.save(Product.builder()
                .name("P1").price(new BigDecimal("10.00")).stockQuantity(1).category(a).build());
        productRepository.save(Product.builder()
                .name("P2").price(new BigDecimal("20.00")).stockQuantity(2).category(a).build());
        productRepository.save(Product.builder()
                .name("P3").price(new BigDecimal("30.00")).stockQuantity(3).category(b).build());
        em.flush();

        Integer updated = TaskTestSupport.invoke(productRepository, "updatePricesInCategory",
                new Class<?>[]{String.class, BigDecimal.class},
                "A", new BigDecimal("99.99"));
        assertThat(updated).isEqualTo(2);

        // Состояние БД (native — мимо persistence context):
        // у обоих товаров категории A новая цена, товар категории B не тронут
        List<Object[]> rows = em.createNativeQuery(
                "SELECT c.name, p.name, p.price FROM products p "
                        + "JOIN categories c ON c.id = p.category_id ORDER BY p.name", Object[].class)
                .getResultList();
        assertThat(rows).hasSize(3);
        assertThat((String) rows.get(0)[0]).isEqualTo("A");
        assertThat((String) rows.get(0)[1]).isEqualTo("P1");
        assertThat(((Number) rows.get(0)[2]).doubleValue()).isCloseTo(99.99, offset(0.001));
        assertThat((String) rows.get(1)[0]).isEqualTo("A");
        assertThat((String) rows.get(1)[1]).isEqualTo("P2");
        assertThat(((Number) rows.get(1)[2]).doubleValue()).isCloseTo(99.99, offset(0.001));
        assertThat((String) rows.get(2)[0]).isEqualTo("B");
        assertThat((String) rows.get(2)[1]).isEqualTo("P3");
        assertThat(((Number) rows.get(2)[2]).doubleValue()).isCloseTo(30.00, offset(0.001));
    }
}
