package com.example.jpalab.tasks.t16;

import com.example.jpalab.entity.Product;
import com.example.jpalab.entity.Tag;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.repository.TagRepository;
import com.example.jpalab.service.ProductTagService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 16, задание 07. N+1 на @ManyToMany: загрузка ВСЕХ продуктов
 * с инициализированными коллекциями тегов ровно ОДНИМ SQL (JOIN FETCH
 * по владельской коллекции). Наивная findAll + ленивая инициализация
 * даёт 1+N запросов — тест красный.
 * Метод-артефакт: ProductTagService.findAllWithTags().
 */
@DataJpaTest
@Import(ProductTagService.class)
class TASK07Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ProductTagService service;

    @Autowired
    private EntityManager entityManager;

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
    void findAllWithTagsUsesSingleFetchQuery() {
        Tag t1 = tagRepository.save(Tag.builder().name("T1").build());
        Tag t2 = tagRepository.save(Tag.builder().name("T2").build());
        saveProduct("P1", t1, t2); // 2 тега
        saveProduct("P2", t1);     // 1 тег
        saveProduct("P3");         // без тегов
        entityManager.flush();
        entityManager.clear();

        Statistics statistics = entityManager.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        List<Product> list = TaskTestSupport.invoke(service, "findAllWithTags", new Class<?>[]{});
        assertThat(list).hasSize(3);

        // принудительное чтение коллекций: без fetch-join здесь были бы
        // доп. SELECT по product_tag_link
        for (Product p : list) {
            p.getProductTags().size();
        }

        // ровно один запрос: products + product_tag_link в одном JOIN
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }
}
