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
 * Тема 16, задание 01. Владелец @ManyToMany: добавление связи из
 * `Product.productTags` создаёт строку в промежуточной таблице
 * `product_tag_link`; Set-семантика — повторное добавление не дублирует.
 * Метод-артефакт: ProductTagService.addTagToProduct(Long, Tag).
 */
@DataJpaTest
@Import(ProductTagService.class)
class TASK01Test {

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
    void ownerSideAddCreatesJoinRow() {
        Product p = productRepository.save(Product.builder().name("P1")
                .price(new BigDecimal("10.00")).stockQuantity(1).build());
        Tag t1 = tagRepository.save(Tag.builder().name("T1").build());
        Tag t2 = tagRepository.save(Tag.builder().name("T2").build());

        TaskTestSupport.invoke(service, "addTagToProduct", new Class<?>[]{Long.class, Tag.class}, p.getId(), t1);
        entityManager.flush();
        assertThat(joinCountForPair(p.getId(), t1.getId())).isEqualTo(1);

        TaskTestSupport.invoke(service, "addTagToProduct", new Class<?>[]{Long.class, Tag.class}, p.getId(), t2);
        entityManager.flush();
        assertThat(joinCountForProduct(p.getId())).isEqualTo(2);

        // повторное добавление того же тега: строка не дублируется
        TaskTestSupport.invoke(service, "addTagToProduct", new Class<?>[]{Long.class, Tag.class}, p.getId(), t1);
        entityManager.flush();
        assertThat(joinCountForPair(p.getId(), t1.getId())).isEqualTo(1);
        assertThat(joinCountForProduct(p.getId())).isEqualTo(2);
    }
}
