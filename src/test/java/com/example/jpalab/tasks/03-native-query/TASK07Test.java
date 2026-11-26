package com.example.jpalab.tasks.t03;

import com.example.jpalab.dto.ProductNamePrice;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тема 03, задание 07. Ловушка: constructor expression в native не работает.
 *
 * Динамические запросы через EntityManager:
 * (а) «SELECT new com.example.jpalab.dto.ProductNamePrice(name, price) FROM products»
 *     как native — завершается исключением: SELECT new ... — синтаксис JPQL,
 *     в native-запросе DTO собирается вручную из Object[]/скаляров;
 * (б) ваш метод `nativeAllNamePrice()` в `ProductRepository` — корректный
 *     native-запрос, возвращающий все пары (name, price) как Object[] -> record.
 */
@DataJpaTest
class TASK07Test {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void constructorExpressionInNative() {
        // данные: 2 товара
        productRepository.save(Product.builder()
                .name("Alpha").price(new BigDecimal("10.00")).stockQuantity(1).build());
        productRepository.save(Product.builder()
                .name("Beta").price(new BigDecimal("20.00")).stockQuantity(1).build());

        // (а) constructor expression в native — исключение
        assertThatThrownBy(() -> em.createNativeQuery(
                "SELECT new com.example.jpalab.dto.ProductNamePrice(name, price) FROM products",
                ProductNamePrice.class)
                .getResultList())
                .isInstanceOf(Exception.class);

        // (б) ваш метод в репозитории: native-выборка пар (name, price)
        List<Object[]> rows = TaskTestSupport.invoke(productRepository, "nativeAllNamePrice",
                new Class<?>[]{}, new Object[0]);
        assertThat(rows).hasSize(2);
        for (Object[] row : rows) {
            assertThat(row).hasSize(2);
            assertThat(row[0]).isInstanceOf(String.class);
            assertThat(row[1]).isInstanceOf(BigDecimal.class);
        }
        List<ProductNamePrice> mapped = rows.stream()
                .map(row -> new ProductNamePrice((String) row[0], (BigDecimal) row[1]))
                .collect(Collectors.toList());
        assertThat(mapped).extracting(ProductNamePrice::name)
                .containsExactlyInAnyOrder("Alpha", "Beta");
        assertThat(mapped).extracting(ProductNamePrice::price)
                .containsExactlyInAnyOrder(new BigDecimal("10.00"), new BigDecimal("20.00"));
    }
}
