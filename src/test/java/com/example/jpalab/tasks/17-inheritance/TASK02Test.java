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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 17, задание 02. Подклассы @MappedSuperclass-корня —
 * самостоятельные сущности: каждая пишет в свою таблицу, и экземпляры
 * обоих типов сосуществуют в одной базе. Метод-артефакт:
 * ProductHierarchyService.savePhysicalProduct(Double, LocalDateTime).
 * До реализации — красный (метод отсутствует).
 */
@DataJpaTest
@Import(ProductHierarchyService.class)
class TASK02Test {

    @Autowired
    private ProductHierarchyService service;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savePhysicalProductCoexistsWithDigital() {
        DigitalProduct digital = new DigitalProduct();
        digital.setDownloadUrl("http://files/app-2.exe");
        entityManager.persist(digital);
        entityManager.flush();

        LocalDateTime createdAt = LocalDateTime.of(2024, 3, 2, 9, 30);
        PhysicalProduct saved = TaskTestSupport.invoke(service, "savePhysicalProduct",
                new Class<?>[]{Double.class, LocalDateTime.class}, 2.75, createdAt);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isNotEqualTo(digital.getId());

        entityManager.clear();
        PhysicalProduct reloaded = entityManager.find(PhysicalProduct.class, saved.getId());
        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getWeight()).isEqualTo(2.75);
        assertThat(reloaded.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entityManager.find(DigitalProduct.class, digital.getId())).isNotNull();
    }
}
