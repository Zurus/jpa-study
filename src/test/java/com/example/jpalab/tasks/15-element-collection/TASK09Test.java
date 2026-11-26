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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 15, задание 09. Слияние (merge) Map&lt;String, String&gt;: новые
 * ключи добавляются, значения существующих обновляются, ключи, которых
 * нет в updates, СОХРАНЯЮТСЯ. «Полная замена» (clear+putAll) потеряет
 * сохранённый ключ и тест будет красным.
 * Метод-артефакт: ProductElementCollectionService.updateAttributes(Long, Map).
 */
@DataJpaTest
@Import(ProductElementCollectionService.class)
class TASK09Test {

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

    private String attrValue(long productId, String key) {
        return (String) entityManager.createNativeQuery(
                        "SELECT attr_value FROM product_attributes WHERE product_id = :pid AND attr_key = :key", String.class)
                .setParameter("pid", productId).setParameter("key", key).getSingleResult();
    }

    @Test
    void updateAttributesMergesInsteadOfReplace() {
        Product p = productRepository.save(Product.builder().name("P1")
                .price(new BigDecimal("10.00")).stockQuantity(1).build());
        p.getAttributes().put("keep", "K");
        p.getAttributes().put("update", "OLD");
        entityManager.flush();
        assertThat(countAttrs(p.getId())).isEqualTo(2);

        TaskTestSupport.invoke(service, "updateAttributes", new Class<?>[]{Long.class, Map.class},
                p.getId(), Map.of("update", "NEW", "add", "A"));
        entityManager.flush();

        // слияние: "keep" сохранён, "update" обновлён, "add" добавлен — 3 строки
        assertThat(countAttrs(p.getId())).isEqualTo(3);
        assertThat(countAttr(p.getId(), "keep")).isEqualTo(1);
        assertThat(attrValue(p.getId(), "keep")).isEqualTo("K");
        assertThat(countAttr(p.getId(), "update")).isEqualTo(1);
        assertThat(attrValue(p.getId(), "update")).isEqualTo("NEW");
        assertThat(countAttr(p.getId(), "add")).isEqualTo(1);
        assertThat(attrValue(p.getId(), "add")).isEqualTo("A");
    }
}
