package com.example.jpalab.tasks.t18;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 18, задание 09 (difficulty 9): @AttributeOverride передаёт не
 * только имя колонки, но и атрибуты @Column: base_currency —
 * VARCHAR(3) NOT NULL.
 */
@DataJpaTest
class TASK09Test {

    @Autowired
    private EntityManager entityManager;

    @Test
    void attributeOverrideCarriesColumnAttributes() {
        List<Object> rows = entityManager.createNativeQuery(
                        "SELECT CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS "
                                + "WHERE TABLE_NAME = 'OFFER' AND COLUMN_NAME = 'BASE_CURRENCY'")
                .getResultList();
        assertThat(rows).as("колонка BASE_CURRENCY в таблице OFFER").hasSize(1);

        Object[] values = (Object[]) rows.get(0);
        assertThat(((Number) values[0]).intValue()).isEqualTo(3);
        assertThat(values[1]).isEqualTo("NO");
    }
}
