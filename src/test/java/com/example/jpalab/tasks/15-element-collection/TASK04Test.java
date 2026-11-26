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
 * Тема 15, задание 04. Удаление пары из Map&lt;String, String&gt; по ключу:
 * исчезает только её строка; удаление отсутствующего ключа — no-op.
 * Метод-артефакт: ProductElementCollectionService.removeAttribute(Long, String).
 */
@DataJpaTest
@Import(ProductElementCollectionService.class)
class TASK04Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductElementCollectionService service;

    @Autowired
    private EntityManager entityManager;

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
    void removeAttributeDeletesRowOnly() {
        Product p = productRepository.save(Product.builder().name("P1")
                .price(new BigDecimal("10.00")).stockQuantity(1).build());
        p.getAttributes().put("color", "red");
        p.getAttributes().put("size", "L");
        entityManager.flush();
        assertThat(countAttrs(p.getId())).isEqualTo(2);

        TaskTestSupport.invoke(service, "removeAttribute", new Class<?>[]{Long.class, String.class}, p.getId(), "color");
        entityManager.flush();
        // удалена только пара "color": "size" на месте
        assertThat(countAttr(p.getId(), "color")).isEqualTo(0);
        assertThat(countAttr(p.getId(), "size")).isEqualTo(1);
        assertThat(countAttrs(p.getId())).isEqualTo(1);

        // удаление отсутствующего ключа: без исключений и без побочных эффектов
        TaskTestSupport.invoke(service, "removeAttribute", new Class<?>[]{Long.class, String.class}, p.getId(), "nope");
        entityManager.flush();
        assertThat(countAttrs(p.getId())).isEqualTo(1);
        assertThat(countAttr(p.getId(), "size")).isEqualTo(1);
    }
}
