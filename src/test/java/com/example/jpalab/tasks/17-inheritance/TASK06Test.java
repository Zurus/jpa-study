package com.example.jpalab.tasks.t17;

import com.example.jpalab.entity.DigitalProduct;
import com.example.jpalab.entity.PhysicalProduct;
import com.example.jpalab.service.ProductHierarchyService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 17, задание 06. Полиморфная таблица и NULL-колонки: цифровой
 * товар в products_base имеет weight = NULL; запрос «физические товары
 * с весом не меньше X» обязан вернуть только физика (запрос по
 * конкретному подклассу — Hibernate сам учитывает дискриминатор).
 * Метод-артефакт: ProductHierarchyService.findPhysicalProductIdsByWeightAtLeast(Double).
 * До реализации — красный (метод отсутствует).
 */
@DataJpaTest
@Import(ProductHierarchyService.class)
class TASK06Test {

    @Autowired
    private ProductHierarchyService service;

    @Autowired
    private EntityManager entityManager;

    private DigitalProduct persistDigital(String url) {
        DigitalProduct product = new DigitalProduct();
        product.setDownloadUrl(url);
        entityManager.persist(product);
        return product;
    }

    private PhysicalProduct persistPhysical(double weight) {
        PhysicalProduct product = new PhysicalProduct();
        product.setWeight(weight);
        entityManager.persist(product);
        return product;
    }

    @Test
    void findPhysicalProductIdsByWeightAtLeastNeverReturnsDigital() {
        DigitalProduct d1 = persistDigital("http://files/1.exe");
        DigitalProduct d2 = persistDigital("http://files/2.exe");
        PhysicalProduct p1 = persistPhysical(1.5);
        PhysicalProduct p2 = persistPhysical(5.0);
        entityManager.flush();

        Set<Long> heavy = TaskTestSupport.invoke(service, "findPhysicalProductIdsByWeightAtLeast",
                new Class<?>[]{Double.class}, 2.0);
        assertThat(heavy).containsExactly(p2.getId());

        Set<Long> all = TaskTestSupport.invoke(service, "findPhysicalProductIdsByWeightAtLeast",
                new Class<?>[]{Double.class}, 0.5);
        assertThat(all).containsExactlyInAnyOrder(p1.getId(), p2.getId());
        assertThat(all).doesNotContain(d1.getId(), d2.getId());
    }
}
