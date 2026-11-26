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
 * Тема 15, задание 05. Поиск товаров по элементу элементной коллекции
 * (join по Set&lt;String&gt;): только точное совпадение тега; неизвестный
 * тег — пусто. Метод-артефакт: ProductElementCollectionService.findProductIdsByTag(String).
 */
@DataJpaTest
@Import(ProductElementCollectionService.class)
class TASK05Test {

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
    void findProductIdsByTagReturnsOnlyMatching() {
        Long p1 = saveProduct("P1", "a");
        Long p2 = saveProduct("P2", "b");
        Long p3 = saveProduct("P3", "a", "b");
        Long p4 = saveProduct("P4"); // без тегов

        Set<Long> byA = TaskTestSupport.invoke(service, "findProductIdsByTag", new Class<?>[]{String.class}, "a");
        assertThat(byA).containsExactlyInAnyOrder(p1, p3);

        Set<Long> byB = TaskTestSupport.invoke(service, "findProductIdsByTag", new Class<?>[]{String.class}, "b");
        assertThat(byB).containsExactlyInAnyOrder(p2, p3);

        Set<Long> none = TaskTestSupport.invoke(service, "findProductIdsByTag", new Class<?>[]{String.class}, "z");
        assertThat(none).isEmpty();
    }
}
