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
 * Тема 15, задание 01. Добавление элемента в Set&lt;String&gt; через
 * @ElementCollection: строка появляется в таблице коллекции;
 * Set-семантика — повторное добавление не дублирует строку.
 * Метод-артефакт: ProductElementCollectionService.addTag(Long, String).
 */
@DataJpaTest
@Import(ProductElementCollectionService.class)
class TASK01Test {

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

    private long countTag(long productId, String tag) {
        return (long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM product_tags WHERE product_id = :pid AND tag = :tag", Long.class)
                .setParameter("pid", productId).setParameter("tag", tag).getSingleResult();
    }

    @Test
    void addTagStoresRowAndIgnoresDuplicate() {
        Product p = productRepository.save(Product.builder().name("P1")
                .price(new BigDecimal("10.00")).stockQuantity(1).build());

        TaskTestSupport.invoke(service, "addTag", new Class<?>[]{Long.class, String.class}, p.getId(), "red");
        entityManager.flush();
        assertThat(countTag(p.getId(), "red")).isEqualTo(1);

        TaskTestSupport.invoke(service, "addTag", new Class<?>[]{Long.class, String.class}, p.getId(), "blue");
        entityManager.flush();
        assertThat(countTags(p.getId())).isEqualTo(2);

        // повторное добавление того же элемента: дубликата строк нет
        TaskTestSupport.invoke(service, "addTag", new Class<?>[]{Long.class, String.class}, p.getId(), "red");
        entityManager.flush();
        assertThat(countTag(p.getId(), "red")).isEqualTo(1);
        assertThat(countTags(p.getId())).isEqualTo(2);
    }
}
