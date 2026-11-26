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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 16, задание 10. Полная синхронизация связей продукта: после
 * вызова в `product_tag_link` у продукта остаются ровно строки
 * переданного набора (старые удалены, новые добавлены); пустой набор
 * обрывает все связи. «Просто добавить» не подходит — старые строки
 * останутся, тест красный.
 * Метод-артефакт: ProductTagService.syncProductTags(Long, Set).
 */
@DataJpaTest
@Import(ProductTagService.class)
class TASK10Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ProductTagService service;

    @Autowired
    private EntityManager entityManager;

    private long joinCountForProduct(long productId) {
        return (long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM product_tag_link WHERE product_id = :pid", Long.class)
                .setParameter("pid", productId).getSingleResult();
    }

    private long joinCountForPair(long productId, long tagId) {
        return (long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM product_tag_link WHERE product_id = :pid AND tag_id = :tid", Long.class)
                .setParameter("pid", productId).setParameter("tid", tagId).getSingleResult();
    }

    @Test
    void syncProductTagsReplacesWholeLinkSet() {
        Tag t1 = tagRepository.save(Tag.builder().name("T1").build());
        Tag t2 = tagRepository.save(Tag.builder().name("T2").build());
        Tag t3 = tagRepository.save(Tag.builder().name("T3").build());
        Product p = productRepository.save(Product.builder().name("P1")
                .price(new BigDecimal("10.00")).stockQuantity(1).build());
        p.getProductTags().add(t1);
        p.getProductTags().add(t2);
        entityManager.flush();
        assertThat(joinCountForProduct(p.getId())).isEqualTo(2);

        // полная замена: t1 отваливается, t3 появляется
        TaskTestSupport.invoke(service, "syncProductTags", new Class<?>[]{Long.class, Set.class},
                p.getId(), Set.of(t2, t3));
        entityManager.flush();
        assertThat(joinCountForPair(p.getId(), t1.getId())).isEqualTo(0);
        assertThat(joinCountForPair(p.getId(), t2.getId())).isEqualTo(1);
        assertThat(joinCountForPair(p.getId(), t3.getId())).isEqualTo(1);
        assertThat(joinCountForProduct(p.getId())).isEqualTo(2);

        // пустой набор — связей нет
        TaskTestSupport.invoke(service, "syncProductTags", new Class<?>[]{Long.class, Set.class},
                p.getId(), Set.of());
        entityManager.flush();
        assertThat(joinCountForProduct(p.getId())).isEqualTo(0);
    }
}
