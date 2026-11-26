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
 * Тема 15, задание 02. Удаление одного элемента из Set&lt;String&gt;:
 * исчезает только его строка; удаление отсутствующего элемента — no-op.
 * Метод-артефакт: ProductElementCollectionService.removeTag(Long, String).
 */
@DataJpaTest
@Import(ProductElementCollectionService.class)
class TASK02Test {

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
    void removeTagDeletesRowOnly() {
        Product p = productRepository.save(Product.builder().name("P1")
                .price(new BigDecimal("10.00")).stockQuantity(1).build());
        p.getTags().add("red");
        p.getTags().add("blue");
        entityManager.flush();
        assertThat(countTags(p.getId())).isEqualTo(2);

        TaskTestSupport.invoke(service, "removeTag", new Class<?>[]{Long.class, String.class}, p.getId(), "red");
        entityManager.flush();
        // удалён только "red": "blue" на месте, общая строка одна
        assertThat(countTag(p.getId(), "red")).isEqualTo(0);
        assertThat(countTag(p.getId(), "blue")).isEqualTo(1);
        assertThat(countTags(p.getId())).isEqualTo(1);

        // удаление отсутствующего элемента: без исключений и без побочных эффектов
        TaskTestSupport.invoke(service, "removeTag", new Class<?>[]{Long.class, String.class}, p.getId(), "nope");
        entityManager.flush();
        assertThat(countTags(p.getId())).isEqualTo(1);
        assertThat(countTag(p.getId(), "blue")).isEqualTo(1);
    }
}
