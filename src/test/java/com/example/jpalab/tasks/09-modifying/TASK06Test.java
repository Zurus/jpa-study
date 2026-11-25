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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 09, задание 06. clearAutomatically: PC должен очиститься после bulk UPDATE.
 *
 * Bulk UPDATE меняет БД, но НЕ persistence context. Без clearAutomatically = true
 * PC продолжает держать «старые» managed-сущности, и findById сразу после
 * обновления вернёт их — со старыми значениями. Тест НЕ вызывает em.clear():
 * обязанность «свежести» переложена на флаг метода.
 */
@DataJpaTest
class TASK06Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void clearAutomaticallyMakesFindFresh() {
        Category a = categoryRepository.saveAndFlush(Category.builder().name("A").build());
        Product p1 = productRepository.save(Product.builder()
                .name("P1").price(new BigDecimal("10.00")).stockQuantity(1).category(a).build());
        Product p2 = productRepository.save(Product.builder()
                .name("P2").price(new BigDecimal("20.00")).stockQuantity(1).category(a).build());
        em.flush();

        // загружаем сущности в persistence context: в памяти 10.00 / 20.00
        Product loaded1 = productRepository.findById(p1.getId()).orElseThrow();
        Product loaded2 = productRepository.findById(p2.getId()).orElseThrow();
        assertThat(loaded1.getPrice()).isEqualByComparingTo("10.00");
        assertThat(loaded2.getPrice()).isEqualByComparingTo("20.00");

        Integer updated = TaskTestSupport.invoke(productRepository, "resetPricesInCategory",
                new Class<?>[]{String.class, BigDecimal.class},
                "A", new BigDecimal("99.99"));
        assertThat(updated).isEqualTo(2);

        // em.clear() в тесте нет: метод обязан сам очистить PC
        // (clearAutomatically = true), тогда повторный find пойдёт в БД
        // и вернёт новую цену
        Product fresh1 = productRepository.findById(p1.getId()).orElseThrow();
        Product fresh2 = productRepository.findById(p2.getId()).orElseThrow();
        assertThat(fresh1.getPrice()).isEqualByComparingTo("99.99");
        assertThat(fresh2.getPrice()).isEqualByComparingTo("99.99");
    }
}
