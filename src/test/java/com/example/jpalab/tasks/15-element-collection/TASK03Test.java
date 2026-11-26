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
 * Тема 15, задание 03. Запись пары Map&lt;String, String&gt; через
 * @ElementCollection + @MapKeyColumn: строка с ключом и значением;
 * повторная запись того же ключа ЗАМЕНЯЕТ значение (строка одна).
 * Метод-артефакт: ProductElementCollectionService.setAttribute(Long, String, String).
 */
@DataJpaTest
@Import(ProductElementCollectionService.class)
class TASK03Test {

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
    void setAttributeStoresKeyValue() {
        Product p = productRepository.save(Product.builder().name("P1")
                .price(new BigDecimal("10.00")).stockQuantity(1).build());

        TaskTestSupport.invoke(service, "setAttribute", new Class<?>[]{Long.class, String.class, String.class},
                p.getId(), "color", "red");
        entityManager.flush();
        assertThat(countAttr(p.getId(), "color")).isEqualTo(1);
        assertThat(attrValue(p.getId(), "color")).isEqualTo("red");

        TaskTestSupport.invoke(service, "setAttribute", new Class<?>[]{Long.class, String.class, String.class},
                p.getId(), "size", "L");
        entityManager.flush();
        assertThat(countAttrs(p.getId())).isEqualTo(2);

        // та же пара ключа: значение заменено, строка не продублирована
        TaskTestSupport.invoke(service, "setAttribute", new Class<?>[]{Long.class, String.class, String.class},
                p.getId(), "color", "blue");
        entityManager.flush();
        assertThat(countAttr(p.getId(), "color")).isEqualTo(1);
        assertThat(attrValue(p.getId(), "color")).isEqualTo("blue");
        assertThat(countAttrs(p.getId())).isEqualTo(2);
    }
}
