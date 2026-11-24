package com.example.jpalab.tasks.t01;

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
 * Тема 01, задание 10. Порядок параметров: два параметра одного типа.
 *
 * Целевой метод (добавить в ProductRepository, без @Query):
 * <pre>{@code
 * List<Product> findByPriceGreaterThanAndPriceLessThanEqual(
 *         BigDecimal lowerPrice, BigDecimal upperPrice);
 * }</pre>
 * price &gt; lowerPrice И price &lt;= upperPrice (нижняя граница исключена,
 * верхняя включена). Порядок параметров определяется порядком условий
 * в имени метода; при «перепутанных» аргументах вызов компилируется,
 * но молча возвращает пустой результат.
 */
@DataJpaTest
class TASK10Test {

    @Autowired
    private ProductRepository productRepository;

    private void save(String name, String price) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .build());
    }

    private List<Product> call(String lower, String upper) {
        return invoke(productRepository,
                "findByPriceGreaterThanAndPriceLessThanEqual",
                new Class<?>[]{BigDecimal.class, BigDecimal.class},
                new BigDecimal(lower), new BigDecimal(upper));
    }

    @Test
    void parameterOrderFollowsMethodName() {
        // товары с ценами 100, 200, 300, 400
        save("Price 100", "100.00");
        save("Price 200", "200.00");
        save("Price 300", "300.00");
        save("Price 400", "400.00");

        // (200, 400): 200 < price <= 400 -> товары 300 и 400.
        // Нижняя граница (200) исключена, верхняя (400) включена.
        List<Product> correctOrder = call("200.00", "400.00");
        assertThat(correctOrder).extracting(Product::getName)
                .containsExactlyInAnyOrder("Price 300", "Price 400");
        assertThat(correctOrder).extracting(Product::getName)
                .doesNotContain("Price 200", "Price 100");

        // (400, 200): аргументы перепутаны местами.
        // Компилятор не может этого поймать (оба параметра BigDecimal):
        // смысл запроса несёт имя метода, а не порядок аргументов вызова.
        // Получаем 400 < price <= 200 — пустой результат без ошибки.
        List<Product> swapped = call("400.00", "200.00");
        assertThat(swapped).isEmpty();
    }
}
