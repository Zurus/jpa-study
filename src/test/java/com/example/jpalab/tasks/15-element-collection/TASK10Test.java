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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 15, задание 10. persist transient-сущности с уже заполненными
 * элементными коллекциями: по умолчанию @ElementCollection каскадирует
 * PERSIST/MERGE, поэтому строки обеих коллекций появляются без отдельной
 * обработки. Метод-артефакт:
 * ProductElementCollectionService.persistProductWithPreparedCollection(Product).
 */
@DataJpaTest
@Import(ProductElementCollectionService.class)
class TASK10Test {

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

    private long countAttrs(long productId) {
        return (long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM product_attributes WHERE product_id = :pid", Long.class)
                .setParameter("pid", productId).getSingleResult();
    }

    private long countAttr(long productId, String key) {
        return (long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM product_attributes WHERE product_id = :pid AND attr_key = :key", Long.class)
                .setParameter("pid", productId).setParameter("key", key).getSingleResult();
    }

    @Test
    void persistProductWithPreparedCollection() {
        // transient-сущность: коллекции заполнены ДО persist
        Product fresh = Product.builder().name("Fresh")
                .price(new BigDecimal("10.00")).stockQuantity(1).build();
        fresh.getTags().add("t1");
        fresh.getTags().add("t2");
        fresh.getAttributes().put("k", "v");
        assertThat(fresh.getId()).isNull();

        TaskTestSupport.invoke(service, "persistProductWithPreparedCollection",
                new Class<?>[]{Product.class}, fresh);
        entityManager.flush();

        // сущность сохранена и строки обеих коллекций за каскад persist
        assertThat(fresh.getId()).isNotNull();
        assertThat(countTags(fresh.getId())).isEqualTo(2);
        assertThat(countAttrs(fresh.getId())).isEqualTo(1);
        assertThat(countAttr(fresh.getId(), "k")).isEqualTo(1);
    }
}
