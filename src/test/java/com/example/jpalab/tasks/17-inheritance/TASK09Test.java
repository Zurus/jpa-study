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
 * Тема 17, задание 09. Ловушка SINGLE_TABLE: в products_base у
 * физического товара download_url = NULL; «цифровые товары без ссылки
 * на скачивание» — это не все строки с NULL в download_url, а только
 * строки с дискриминатором DIGITAL (запрос по конкретному подклассу).
 * Метод-артефакт: ProductHierarchyService.findDigitalProductIdsWithoutDownloadUrl().
 * До реализации — красный (метод отсутствует).
 */
@DataJpaTest
@Import(ProductHierarchyService.class)
class TASK09Test {

    @Autowired
    private ProductHierarchyService service;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findDigitalProductIdsWithoutDownloadUrlIgnoresPhysicalRows() {
        DigitalProduct withUrl = new DigitalProduct();
        withUrl.setDownloadUrl("http://files/with.exe");
        entityManager.persist(withUrl);

        DigitalProduct withoutUrl = new DigitalProduct();
        entityManager.persist(withoutUrl);

        PhysicalProduct physical = new PhysicalProduct();
        physical.setWeight(7.5);
        entityManager.persist(physical);
        entityManager.flush();

        Set<Long> result = TaskTestSupport.invoke(service, "findDigitalProductIdsWithoutDownloadUrl",
                new Class<?>[]{});
        assertThat(result).containsExactly(withoutUrl.getId());
    }
}
