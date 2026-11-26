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
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 16, задание 08. Продукты, имеющие общий с данным продуктом тег
 * (сам продукт в результат НЕ входит). Продукт, общий по двум тегам,
 * возвращается один раз (distinct). У продукта без общих тегов — пусто.
 * Метод-артефакт: ProductTagService.findProductsSharingTagWith(Long).
 */
@DataJpaTest
@Import(ProductTagService.class)
class TASK08Test {

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
    void findProductsSharingTagWithExcludesSelf() {
        Tag a = saveTag("A");
        Tag b = saveTag("B");
        Tag c = saveTag("C");
        Long p1 = saveProduct("P1", a, b); // «центр»
        Long p2 = saveProduct("P2", a);
        Long p3 = saveProduct("P3", b);
        Long p4 = saveProduct("P4", c);    // общих тегов с p1 нет
        Long p5 = saveProduct("P5", a, b); // общих с p1 ДВА тега — но вернуть один раз

        List<Product> shared = TaskTestSupport.invoke(service, "findProductsSharingTagWith",
                new Class<?>[]{Long.class}, p1);
        List<Long> sharedIds = shared.stream().map(Product::getId).collect(Collectors.toList());
        assertThat(sharedIds).containsExactlyInAnyOrder(p2, p3, p5);

        List<Product> none = TaskTestSupport.invoke(service, "findProductsSharingTagWith",
                new Class<?>[]{Long.class}, p4);
        assertThat(none).isEmpty();
    }
}
