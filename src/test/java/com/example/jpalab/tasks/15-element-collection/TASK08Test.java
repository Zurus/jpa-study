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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 15, задание 08. Полная синхронизация Set&lt;String&gt;: результат —
 * ровно переданный набор (старые элементы удалены, новые добавлены);
 * пустой набор очищает коллекцию. «Добавить поверх» не подходит —
 * старые строки останутся и тест красный.
 * Метод-артефакт: ProductElementCollectionService.syncTags(Long, Set).
 */
@DataJpaTest
@Import(ProductElementCollectionService.class)
class TASK08Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductElementCollectionService service;

    @Autowired
    private EntityManager entityManager;

    private long countTags(long productId) {
        return (long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM product_tags WHERE product_id = :pid", Long.class)
                .setParameter("pid", productId).getSingleResult();
    }

    private Set<String> tagValues(long productId) {
        List<Object> rows = entityManager.createNativeQuery(
                        "SELECT tag FROM product_tags WHERE product_id = :pid", String.class)
                .setParameter("pid", productId).getResultList();
        Set<String> result = new HashSet<>();
        for (Object row : rows) {
            result.add(String.valueOf(row));
        }
        return result;
    }

    @Test
    void syncTagsReplacesWholeSet() {
        Product p = productRepository.save(Product.builder().name("P1")
                .price(new BigDecimal("10.00")).stockQuantity(1).build());
        p.getTags().add("old1");
        p.getTags().add("old2");
        entityManager.flush();
        assertThat(countTags(p.getId())).isEqualTo(2);

        // полная замена: старые теги исчезают, остаются ровно новые
        TaskTestSupport.invoke(service, "syncTags", new Class<?>[]{Long.class, Set.class},
                p.getId(), Set.of("new1", "new2"));
        entityManager.flush();
        assertThat(tagValues(p.getId())).containsExactlyInAnyOrder("new1", "new2");
        assertThat(countTags(p.getId())).isEqualTo(2);

        // пустой набор — коллекция очищена
        TaskTestSupport.invoke(service, "syncTags", new Class<?>[]{Long.class, Set.class},
                p.getId(), Set.of());
        entityManager.flush();
        assertThat(countTags(p.getId())).isEqualTo(0);
    }
}
