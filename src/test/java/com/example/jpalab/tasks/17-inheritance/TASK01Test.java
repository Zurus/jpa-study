package com.example.jpalab.tasks.t17;

import com.example.jpalab.entity.DigitalProduct;
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
 * Тема 17, задание 01. @MappedSuperclass: поля базового класса
 * (id, createdAt) маппятся через подкласс — цифровому товару не нужно
 * объявлять их заново. Метод-артефакт:
 * ProductHierarchyService.saveDigitalProduct(String, LocalDateTime).
 * До реализации — красный (метод отсутствует).
 */
@DataJpaTest
@Import(ProductHierarchyService.class)
class TASK01Test {

    @Autowired
    private ProductHierarchyService service;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveDigitalProductRoundTripsBaseFields() {
        LocalDateTime createdAt = LocalDateTime.of(2024, 3, 1, 12, 0);

        DigitalProduct saved = TaskTestSupport.invoke(service, "saveDigitalProduct",
                new Class<?>[]{String.class, LocalDateTime.class}, "http://files/app-1.exe", createdAt);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();

        entityManager.clear();
        DigitalProduct reloaded = entityManager.find(DigitalProduct.class, saved.getId());
        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getDownloadUrl()).isEqualTo("http://files/app-1.exe");
        assertThat(reloaded.getCreatedAt()).isEqualTo(createdAt);
    }
}
