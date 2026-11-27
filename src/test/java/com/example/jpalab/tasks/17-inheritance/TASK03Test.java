package com.example.jpalab.tasks.t17;

import com.example.jpalab.entity.DigitalProduct;
import com.example.jpalab.entity.PhysicalProduct;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 17, задание 03. Стратегия SINGLE_TABLE: вся иерархия хранится
 * в ОДНОЙ общей таблице products_base с колонкой-дискриминатором ptype,
 * отдельные таблицы digital_product/physical_product исчезают.
 * Артефакт — аннотации на сущностях (корень становится @Entity-корнем
 * иерархии). До реализации — красный (таблицы products_base нет).
 */
@DataJpaTest
class TASK03Test {

    @Autowired
    private EntityManager entityManager;

    private long count(String sql) {
        return (Long) entityManager.createNativeQuery(sql).getSingleResult();
    }

    @Test
    void singleTableOneSharedTableWithDiscriminator() {
        DigitalProduct digital = new DigitalProduct();
        digital.setDownloadUrl("http://files/app-3.exe");
        entityManager.persist(digital);
        PhysicalProduct physical = new PhysicalProduct();
        physical.setWeight(3.5);
        entityManager.persist(physical);
        entityManager.flush();

        // оба типа — в одной общей таблице
        assertThat(count("SELECT COUNT(*) FROM products_base")).isEqualTo(2L);
        // дискриминатор заполнен у каждой строки
        assertThat(count("SELECT COUNT(*) FROM products_base WHERE ptype IS NOT NULL")).isEqualTo(2L);
        // у каждого типа заполнена только «своя» колонка
        assertThat(count("SELECT COUNT(*) FROM products_base"
                + " WHERE download_url IS NOT NULL AND weight IS NULL")).isEqualTo(1L);
        assertThat(count("SELECT COUNT(*) FROM products_base"
                + " WHERE weight IS NOT NULL AND download_url IS NULL")).isEqualTo(1L);
        // отдельные таблицы подклассов исчезли
        assertThat(count("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES"
                + " WHERE TABLE_NAME IN ('DIGITAL_PRODUCT', 'PHYSICAL_PRODUCT')")).isZero();
    }
}
