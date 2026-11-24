package com.example.jpalab.tasks.t03;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.util.List;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тема 03, задание 02. Явный список колонок и ORDER BY (snake_case).
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query(value = """
 *       SELECT id, name, price, stock_quantity FROM products
 *       WHERE price >= :min ORDER BY name ASC, id ASC
 *       """, nativeQuery = true)
 * List<Product> findNativePricedAtLeastWithColumns(@Param("min") BigDecimal min);
 * }</pre>
 * В native SQL живёт только то, что описано в DDL: stock_quantity, category_id.
 * Имя свойства stockQuantity в SQL-контексте не существует — отдельная
 * часть теста фиксирует ошибку БД («unknown column»).
 */
@DataJpaTest
class TASK02Test {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void explicitColumnListWithSnakeCaseAndIdTieBreaker() {
        // 2 товара с ОДИНАКОВЫМ именем (name не unique) + один дорогой
        Product first = productRepository.save(Product.builder()
                .name("Twin").price(new BigDecimal("100.00")).stockQuantity(1).build());
        Product second = productRepository.save(Product.builder()
                .name("Twin").price(new BigDecimal("200.00")).stockQuantity(2).build());
        productRepository.save(Product.builder()
                .name("Solo").price(new BigDecimal("300.00")).stockQuantity(3).build());

        List<Product> rows = invoke(productRepository,
                "findNativePricedAtLeastWithColumns", new Class<?>[]{BigDecimal.class},
                new BigDecimal("100.00"));

        assertThat(rows).hasSize(3);
        // ORDER BY name ASC, id ASC: два "Twin" — в порядке возрастающего id
        assertThat(rows.get(0).getName()).isEqualTo("Twin");
        assertThat(rows.get(1).getName()).isEqualTo("Twin");
        assertThat(rows.get(0).getId()).isEqualTo(first.getId());
        assertThat(rows.get(1).getId()).isEqualTo(second.getId());
        assertThat(rows.get(2).getName()).isEqualTo("Solo");

        // все поля сущности заполнены (включая stock_quantity из DDL)
        for (Product product : rows) {
            assertThat(product.getPrice()).isNotNull();
            assertThat(product.getStockQuantity()).isIn(1, 2, 3);
        }

        // отдельная часть: имя свойства в native SQL не существует —
        // динамический em.createNativeQuery с "stockQuantity" упадёт
        // с ошибкой БД: unknown column
        assertThatThrownBy(() -> em.createNativeQuery(
                "SELECT id, name, price, stockQuantity FROM products WHERE price >= 0",
                Product.class)
                .getResultList())
                .isInstanceOf(DataAccessException.class)
                .rootCause()
                .hasMessageContaining("not found");
    }
}
