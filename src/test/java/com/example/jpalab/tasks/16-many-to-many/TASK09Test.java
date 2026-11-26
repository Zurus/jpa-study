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
 * Тема 16, задание 09. Общие теги двух продуктов — пересечение их
 * множеств (по именам): только теги, которые есть и у первого, и у
 второго; симметрично по аргументам; без общих тегов — пусто.
 * Метод-артефакт: ProductTagService.findSharedTagNames(Long, Long).
 */
@DataJpaTest
@Import(ProductTagService.class)
class TASK09Test {

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
    void findSharedTagNamesIsIntersection() {
        Tag a = saveTag("A");
        Tag b = saveTag("B");
        Tag c = saveTag("C");
        Tag d = saveTag("D");
        Long p1 = saveProduct("P1", a, b);
        Long p2 = saveProduct("P2", b, c);
        Long p3 = saveProduct("P3", d);

        // пересечение {A,B} и {B,C} — только B
        Set<String> s1 = TaskTestSupport.invoke(service, "findSharedTagNames",
                new Class<?>[]{Long.class, Long.class}, p1, p2);
        assertThat(s1).containsExactlyInAnyOrder("B");

        // симметрия: порядок аргументов не важен
        Set<String> s2 = TaskTestSupport.invoke(service, "findSharedTagNames",
                new Class<?>[]{Long.class, Long.class}, p2, p1);
        assertThat(s2).containsExactlyInAnyOrder("B");

        // общих тегов нет — пусто
        Set<String> s3 = TaskTestSupport.invoke(service, "findSharedTagNames",
                new Class<?>[]{Long.class, Long.class}, p1, p3);
        assertThat(s3).isEmpty();
    }
}
