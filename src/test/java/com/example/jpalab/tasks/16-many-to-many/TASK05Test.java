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
 * Тема 16, задание 05. Удаление Tag: у @ManyToMany по умолчанию
 * cascade = NONE — продукты НЕ удаляются, исчезают только строки
 * связей тега. Связи продуктов с другими тегами не трогаются.
 * Метод-артефакт: ProductTagService.deleteTagKeepsProducts(Long tagId).
 */
@DataJpaTest
@Import(ProductTagService.class)
class TASK05Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ProductTagService service;

    @Autowired
    private EntityManager entityManager;

    private long joinCountForTag(long tagId) {
        return (long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM product_tag_link WHERE tag_id = :tid", Long.class)
                .setParameter("tid", tagId).getSingleResult();
    }

    private long joinCountForPair(long productId, long tagId) {
        return (long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM product_tag_link WHERE product_id = :pid AND tag_id = :tid", Long.class)
                .setParameter("pid", productId).setParameter("tid", tagId).getSingleResult();
    }

    private long joinCountForProduct(long productId) {
        return (long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM product_tag_link WHERE product_id = :pid", Long.class)
                .setParameter("pid", productId).getSingleResult();
    }

    @Test
    void deleteTagRemovesLinksButKeepsProducts() {
        Tag t1 = tagRepository.save(Tag.builder().name("T1").build());
        Tag t2 = tagRepository.save(Tag.builder().name("T2").build());
        Product p1 = productRepository.save(Product.builder().name("P1")
                .price(new BigDecimal("10.00")).stockQuantity(1).build());
        Product p2 = productRepository.save(Product.builder().name("P2")
                .price(new BigDecimal("20.00")).stockQuantity(1).build());
        p1.getProductTags().add(t1);
        p1.getProductTags().add(t2);
        p2.getProductTags().add(t1);
        entityManager.flush();
        assertThat(joinCountForTag(t1.getId())).isEqualTo(2);

        TaskTestSupport.invoke(service, "deleteTagKeepsProducts", new Class<?>[]{Long.class}, t1.getId());
        entityManager.flush();

        // тег удалён, строки его связей исчезли
        assertThat(tagRepository.findById(t1.getId())).isEmpty();
        assertThat(joinCountForTag(t1.getId())).isEqualTo(0);

        // cascade = NONE: продукты на месте
        Product p1r = entityManager.find(Product.class, p1.getId());
        Product p2r = entityManager.find(Product.class, p2.getId());
        assertThat(p1r).isNotNull();
        assertThat(p2r).isNotNull();
        // чужие связи не пострадали: p1-t2 на месте, у p2 связей больше нет
        assertThat(joinCountForPair(p1.getId(), t2.getId())).isEqualTo(1);
        assertThat(joinCountForProduct(p2.getId())).isEqualTo(0);
    }
}
