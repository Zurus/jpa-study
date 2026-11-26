package com.example.jpalab.tasks.t15;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.service.ProductElementCollectionService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 15, задание 06. Поиск товаров по паре ключ/значение Map-коллекции
 * (@MapKeyColumn): совпадают только точные пары; неизвестная пара — пусто.
 * Метод-артефакт: ProductElementCollectionService.findProductIdsByAttribute(String, String).
 */
@DataJpaTest
@Import(ProductElementCollectionService.class)
class TASK06Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductElementCollectionService service;

    @Autowired
    private EntityManager entityManager;

    private Long saveProduct(String name, String... keyValues) {
        Product p = productRepository.save(Product.builder().name(name)
                .price(new BigDecimal("10.00")).stockQuantity(1).build());
        for (int i = 0; i < keyValues.length; i += 2) {
            p.getAttributes().put(keyValues[i], keyValues[i + 1]);
        }
        entityManager.flush();
        return p.getId();
    }

    @Test
    void findProductIdsByAttributeKeyAndValue() {
        Long p1 = saveProduct("P1", "color", "red", "size", "L");
        Long p2 = saveProduct("P2", "color", "blue");
        Long p3 = saveProduct("P3", "color", "red", "size", "XL");

        Set<Long> red = TaskTestSupport.invoke(service, "findProductIdsByAttribute",
                new Class<?>[]{String.class, String.class}, "color", "red");
        assertThat(red).containsExactlyInAnyOrder(p1, p3);

        // ключ без учёта значения не подходит: "size=L" — только P1
        Set<Long> sizeL = TaskTestSupport.invoke(service, "findProductIdsByAttribute",
                new Class<?>[]{String.class, String.class}, "size", "L");
        assertThat(sizeL).containsExactlyInAnyOrder(p1);

        Set<Long> none = TaskTestSupport.invoke(service, "findProductIdsByAttribute",
                new Class<?>[]{String.class, String.class}, "color", "green");
        assertThat(none).isEmpty();

        Set<Long> unknownKey = TaskTestSupport.invoke(service, "findProductIdsByAttribute",
                new Class<?>[]{String.class, String.class}, "nokey", "x");
        assertThat(unknownKey).isEmpty();
    }
}
