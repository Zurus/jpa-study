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
 * Тема 16, задание 06. Поиск продуктов по имени тега (join через
 * @ManyToMany + Tag.name): только точное имя, каждый продукт один раз;
 * неизвестный тег — пусто.
 * Метод-артефакт: ProductTagService.findProductIdsByTagName(String).
 */
@DataJpaTest
@Import(ProductTagService.class)
class TASK06Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ProductTagService service;

    @Autowired
    private EntityManager entityManager;

    private Tag saveTag(String name) {
        return tagRepository.save(Tag.builder().name(name).build());
    }

    private Long saveProduct(String name, Tag... tags) {
        Product p = productRepository.save(Product.builder().name(name)
                .price(new BigDecimal("10.00")).stockQuantity(1).build());
        for (Tag tag : tags) {
            p.getProductTags().add(tag);
        }
        entityManager.flush();
        return p.getId();
    }

    @Test
    void findProductIdsByTagNameReturnsOnlyLinked() {
        Tag a = saveTag("A");
        Tag b = saveTag("B");
        Tag c = saveTag("C");
        Long p1 = saveProduct("P1", a, b);
        Long p2 = saveProduct("P2", a);
        Long p3 = saveProduct("P3", c);
        Long p4 = saveProduct("P4"); // без тегов

        Set<Long> byA = TaskTestSupport.invoke(service, "findProductIdsByTagName", new Class<?>[]{String.class}, "A");
        assertThat(byA).containsExactlyInAnyOrder(p1, p2);

        Set<Long> byC = TaskTestSupport.invoke(service, "findProductIdsByTagName", new Class<?>[]{String.class}, "C");
        assertThat(byC).containsExactlyInAnyOrder(p3);

        Set<Long> none = TaskTestSupport.invoke(service, "findProductIdsByTagName", new Class<?>[]{String.class}, "Z");
        assertThat(none).isEmpty();
    }
}
