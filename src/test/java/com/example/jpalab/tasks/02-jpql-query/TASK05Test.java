package com.example.jpalab.tasks.t02;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 02, задание 05. Проекция в Object[].
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("SELECT p.id, p.name, p.price FROM Product p WHERE p.price > :minPrice")
 * List<Object[]> idNamePriceAbove(@Param("minPrice") BigDecimal minPrice);
 * }</pre>
 * Каждая строка — Object[] из трёх значений в порядке SELECT:
 * [0] Long id, [1] String name, [2] BigDecimal price.
 */
@DataJpaTest
class TASK05Test {

    @Autowired
    private ProductRepository productRepository;

    private Product save(String name, String price) {
        return productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .build());
    }

    @Test
    void objectArrayProjectionHasThreeColumnsInSelectOrder() {
        Product a = save("Row A", "100.00");
        Product b = save("Row B", "200.00");
        Product c = save("Row C", "300.00");

        List<Object[]> rows = invoke(productRepository,
                "idNamePriceAbove", new Class<?>[]{BigDecimal.class}, new BigDecimal("150.00"));

        // строк ровно столько, сколько товаров дороже 150 (дублей нет)
        assertThat(rows).hasSize(2);

        // размер строки 3; индексы соответствуют порядку SELECT
        for (Object[] row : rows) {
            assertThat(row).hasSize(3);
            assertThat(row[0]).isInstanceOf(Long.class);
            assertThat(row[1]).isInstanceOf(String.class);
            assertThat(row[2]).isInstanceOf(BigDecimal.class);
        }

        // id и name в строках соответствуют сохранённым товарам (ровно B и C)
        Map<Long, Product> byId = new HashMap<>();
        byId.put(a.getId(), a);
        byId.put(b.getId(), b);
        byId.put(c.getId(), c);
        assertThat(rows).extracting(row -> (Long) row[0])
                .containsExactlyInAnyOrder(b.getId(), c.getId());
        for (Object[] row : rows) {
            Product product = byId.get((Long) row[0]);
            assertThat((String) row[1]).isEqualTo(product.getName());
            assertThat((BigDecimal) row[2]).isEqualByComparingTo(product.getPrice());
        }
        // товар дешевле 150 в выборке нет
        assertThat(rows).extracting(row -> (Long) row[0]).doesNotContain(a.getId());
    }
}
