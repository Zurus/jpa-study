package com.example.jpalab.tasks.t03;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 03, задание 05. Одна колонка — скалярный результат.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query(value = "SELECT price FROM products WHERE name = :name", nativeQuery = true)
 * List<BigDecimal> nativePricesByName(@Param("name") String name);
 * }</pre>
 * Одна колонка автоматически маппится в скаляр (BigDecimal/String/Long…),
 * а не в Object[] — тип возвращаемого списка определяет упаковку.
 */
@DataJpaTest
class TASK05Test {

    @Autowired
    private ProductRepository productRepository;

    private void save(String name, String price) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .build());
    }

    @Test
    void singleColumnIsScalarResult() {
        // один товар с таким именем, один — с похожим
        save("Widget", "42.50");
        save("Widgets", "99.00");

        // по «Widget» — список из одного BigDecimal(42.50), без сущности-обёртки
        List<BigDecimal> widgetPrices = invoke(productRepository,
                "nativePricesByName", new Class<?>[]{String.class}, "Widget");
        assertThat(widgetPrices).hasSize(1);
        assertThat(widgetPrices.get(0)).isEqualByComparingTo("42.50");

        // «Widgets» (похожее, но другое имя) — своя цена
        List<BigDecimal> widgetsPrices = invoke(productRepository,
                "nativePricesByName", new Class<?>[]{String.class}, "Widgets");
        assertThat(widgetsPrices).hasSize(1);
        assertThat(widgetsPrices.get(0)).isEqualByComparingTo("99.00");

        // несуществующее имя — пустой список
        List<BigDecimal> missing = invoke(productRepository,
                "nativePricesByName", new Class<?>[]{String.class}, "Nope");
        assertThat(missing).isEmpty();
    }
}
