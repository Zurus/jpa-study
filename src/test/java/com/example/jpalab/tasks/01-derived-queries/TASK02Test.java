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
 * Тема 01, задание 02. Поиск без учёта регистра (IgnoreCase).
 *
 * Целевой метод (добавить в ProductRepository, без @Query):
 * <pre>{@code
 * List<Product> findByNameIgnoreCase(String name);
 * }</pre>
 * Тест находит товар с именем в смешанном регистре, передавая имя
 * в нижнем и верхнем регистрах. Это точное совпадение, а не «contains».
 */
@DataJpaTest
class TASK02Test {

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
    void findByNameIgnoreCaseIsCaseInsensitive() {
        Product target = save("Super Widget", "10.00");
        // помеха: имя начинается с того же префикса, но длиннее
        save("Super Widget Plus", "11.00");

        // нижний регистр
        List<Product> lowerCase = invoke(productRepository,
                "findByNameIgnoreCase", new Class<?>[]{String.class}, "super widget");
        assertThat(lowerCase).hasSize(1);
        assertThat(lowerCase.get(0).getId()).isEqualTo(target.getId());
        assertThat(lowerCase.get(0).getName()).isEqualTo("Super Widget");

        // верхний регистр
        List<Product> upperCase = invoke(productRepository,
                "findByNameIgnoreCase", new Class<?>[]{String.class}, "SUPER WIDGET");
        assertThat(upperCase).hasSize(1);
        assertThat(upperCase.get(0).getId()).isEqualTo(target.getId());

        // несуществующее имя -> пустой список
        List<Product> missing = invoke(productRepository,
                "findByNameIgnoreCase", new Class<?>[]{String.class}, "no such product");
        assertThat(missing).isEmpty();
    }
}
