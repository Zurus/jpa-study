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
 * Тема 16, задание 04. Разрыв двунаправленной связи: после unlink в том
 * же persistence context обе стороны пусты (изменение только владельца
 * оставляет «висячую» ссылку в Tag.products — тест красный), а строки
 * в `product_tag_link` нет.
 * Метод-артефакт: ProductTagService.unlinkBothSides(Long, Tag).
 */
@DataJpaTest
@Import(ProductTagService.class)
class TASK04Test {

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
    void unlinkBothSidesEmptyFromBothDirections() {
        Product p = productRepository.save(Product.builder().name("P1")
                .price(new BigDecimal("10.00")).stockQuantity(1).build());
        Tag t = tagRepository.save(Tag.builder().name("T1").build());
        // связь установлена с обеих сторон
        p.getProductTags().add(t);
        t.getProducts().add(p);
        entityManager.flush();
        assertThat(joinCountForPair(p.getId(), t.getId())).isEqualTo(1);

        TaskTestSupport.invoke(service, "unlinkBothSides", new Class<?>[]{Long.class, Tag.class}, p.getId(), t);
        entityManager.flush();

        // строки связи больше нет
        assertThat(joinCountForPair(p.getId(), t.getId())).isEqualTo(0);

        // в том же persistence context обе стороны пусты:
        // удаление только из владельца оставило бы ссылку в t.getProducts()
        assertThat(p.getProductTags()).isEmpty();
        assertThat(t.getProducts()).isEmpty();

        // и в чистом контексте обе стороны пусты
        entityManager.clear();
        Product p2 = entityManager.find(Product.class, p.getId());
        Tag t2 = entityManager.find(Tag.class, t.getId());
        assertThat(p2.getProductTags()).isEmpty();
        assertThat(t2.getProducts()).isEmpty();
    }
}
