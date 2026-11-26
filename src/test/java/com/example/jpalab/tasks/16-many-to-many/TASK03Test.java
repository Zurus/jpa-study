package com.example.jpalab.tasks.t16;

import com.example.jpalab.entity.Product;
import com.example.jpalab.entity.Tag;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.repository.TagRepository;
import com.example.jpalab.service.ProductTagService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 16, задание 03. Двунаправленная связь: после связывания в том
 * же persistence context связь видна с ОБЕИХ сторон — и в
 * Product.productTags, и в Tag.products. Изменение только владельца
 * оставляет Tag.products устаревшим (пустым) — тест красный. Строка
 * в `product_tag_link` тоже обязательна.
 * Метод-артефакт: ProductTagService.linkBothSides(Long, Tag).
 */
@DataJpaTest
@Import(ProductTagService.class)
class TASK03Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ProductTagService service;

    @Autowired
    private EntityManager entityManager;

    private long joinCountForPair(long productId, long tagId) {
        return (long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM product_tag_link WHERE product_id = :pid AND tag_id = :tid", Long.class)
                .setParameter("pid", productId).setParameter("tid", tagId).getSingleResult();
    }

    @Test
    void linkBothSidesVisibleFromBothDirections() {
        Product p = productRepository.save(Product.builder().name("P1")
                .price(new BigDecimal("10.00")).stockQuantity(1).build());
        Tag t = tagRepository.save(Tag.builder().name("T1").build());

        TaskTestSupport.invoke(service, "linkBothSides", new Class<?>[]{Long.class, Tag.class}, p.getId(), t);
        entityManager.flush();

        // строка связи в БД
        assertThat(joinCountForPair(p.getId(), t.getId())).isEqualTo(1);

        // в том же persistence context связь видна с обеих сторон:
        // изменение только владельца оставило бы t.getProducts() пустым
        assertThat(p.getProductTags()).contains(t);
        assertThat(t.getProducts()).contains(p);

        // и связь сохранена в БД (проверка после чистого контекста)
        entityManager.clear();
        Product p2 = entityManager.find(Product.class, p.getId());
        Tag t2 = entityManager.find(Tag.class, t.getId());
        assertThat(p2.getProductTags()).contains(t2);
        assertThat(t2.getProducts()).contains(p2);
    }
}
