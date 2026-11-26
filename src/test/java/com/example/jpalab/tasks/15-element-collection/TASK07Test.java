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
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 15, задание 07. Поиск товаров по ЛЮБОМУ из списка тегов (union
 * по элементам коллекции): пустой список и полностью неизвестные теги — пусто.
 * Метод-артефакт: ProductElementCollectionService.findProductIdsWithAnyTag(List).
 */
@DataJpaTest
@Import(ProductElementCollectionService.class)
class TASK07Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductElementCollectionService service;

    @Autowired
    private EntityManager entityManager;

    private Long saveProduct(String name, String... tags) {
        Product p = productRepository.save(Product.builder().name(name)
                .price(new BigDecimal("10.00")).stockQuantity(1).build());
        for (String tag : tags) {
            p.getTags().add(tag);
        }
        entityManager.flush();
        return p.getId();
    }

    @Test
    void findProductIdsWithAnyTagIsUnion() {
        Long p1 = saveProduct("P1", "a");
        Long p2 = saveProduct("P2", "b");
        Long p3 = saveProduct("P3", "c");
        Long p4 = saveProduct("P4", "b", "c");

        // union: "a" даёт P1, "z" — никто
        Set<Long> aOrZ = TaskTestSupport.invoke(service, "findProductIdsWithAnyTag",
                new Class<?>[]{List.class}, List.of("a", "z"));
        assertThat(aOrZ).containsExactlyInAnyOrder(p1);

        // union по двум существующим: P2, P3, P4 (P4 — по обоим)
        Set<Long> bOrC = TaskTestSupport.invoke(service, "findProductIdsWithAnyTag",
                new Class<?>[]{List.class}, List.of("b", "c"));
        assertThat(bOrC).containsExactlyInAnyOrder(p2, p3, p4);

        // пустой список — пусто
        Set<Long> empty = TaskTestSupport.invoke(service, "findProductIdsWithAnyTag",
                new Class<?>[]{List.class}, List.of());
        assertThat(empty).isEmpty();

        // полностью неизвестные — пусто
        Set<Long> unknown = TaskTestSupport.invoke(service, "findProductIdsWithAnyTag",
                new Class<?>[]{List.class}, List.of("zzz"));
        assertThat(unknown).isEmpty();
    }
}
