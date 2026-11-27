package com.example.jpalab.tasks.t17;

import com.example.jpalab.entity.BaseEntity;
import com.example.jpalab.entity.DigitalProduct;
import com.example.jpalab.entity.PhysicalProduct;
import com.example.jpalab.service.ProductHierarchyService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 17, задание 05. Полиморфный запрос по корню иерархии:
 * один запрос возвращает оба подкласса в материализованном виде
 * (Hibernate сам подставляет дискриминатор). Метод-артефакт:
 * ProductHierarchyService.findAllProducts() — все товары, отсортированы по id.
 * До реализации — красный (метод отсутствует).
 */
@DataJpaTest
@Import(ProductHierarchyService.class)
class TASK05Test {

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
    void findAllProductsBothTypesOrderedById() {
        DigitalProduct d1 = persistDigital("http://files/1.exe");
        PhysicalProduct p1 = persistPhysical(1.0);
        DigitalProduct d2 = persistDigital("http://files/2.exe");
        PhysicalProduct p2 = persistPhysical(2.0);
        entityManager.flush();

        entityManager.clear();
        List<?> found = TaskTestSupport.invoke(service, "findAllProducts", new Class<?>[]{});
        assertThat(found).hasSize(4);

        assertThat(found.get(0)).isInstanceOf(DigitalProduct.class);
        assertThat(found.get(1)).isInstanceOf(PhysicalProduct.class);
        assertThat(found.get(2)).isInstanceOf(DigitalProduct.class);
        assertThat(found.get(3)).isInstanceOf(PhysicalProduct.class);

        assertThat(((DigitalProduct) found.get(0)).getId()).isEqualTo(d1.getId());
        assertThat(((PhysicalProduct) found.get(1)).getId()).isEqualTo(p1.getId());
        assertThat(((DigitalProduct) found.get(2)).getId()).isEqualTo(d2.getId());
        assertThat(((PhysicalProduct) found.get(3)).getId()).isEqualTo(p2.getId());
    }
}
