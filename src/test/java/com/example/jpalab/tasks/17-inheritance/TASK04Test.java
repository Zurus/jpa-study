package com.example.jpalab.tasks.t17;

import com.example.jpalab.entity.DigitalProduct;
import com.example.jpalab.entity.PhysicalProduct;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 17, задание 04. @DiscriminatorValue: значения колонки-дискриминатора
 * задаются явно — «DIGITAL» и «PHYSICAL» (по умолчанию Hibernate пишет
 * имена классов, тест ловит это). Артефакт — аннотации на подклассах.
 * До реализации — красный.
 */
@DataJpaTest
class TASK04Test {

    @Autowired
    private EntityManager entityManager;

    @Test
    void explicitDiscriminatorValuesInColumn() {
        DigitalProduct digital = new DigitalProduct();
        digital.setDownloadUrl("http://files/app-4.exe");
        entityManager.persist(digital);
        PhysicalProduct physical = new PhysicalProduct();
        physical.setWeight(4.25);
        entityManager.persist(physical);
        entityManager.flush();

        Object digitalValue = entityManager.createNativeQuery(
                        "SELECT ptype FROM products_base WHERE id = :id")
                .setParameter("id", digital.getId()).getSingleResult();
        assertThat(digitalValue).isEqualTo("DIGITAL");

        Object physicalValue = entityManager.createNativeQuery(
                        "SELECT ptype FROM products_base WHERE id = :id")
                .setParameter("id", physical.getId()).getSingleResult();
        assertThat(physicalValue).isEqualTo("PHYSICAL");
    }
}
