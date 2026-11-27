package com.example.jpalab.tasks.t18;

import com.example.jpalab.entity.Money;
import com.example.jpalab.entity.Offer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 18, задание 02 (difficulty 2): @AttributeOverrides у
 * Offer.basePrice переименовывает колонки в base_amount/base_currency;
 * дефолтные колонки amount/currency исчезают.
 */
@DataJpaTest
class TASK02Test {

    @Autowired
    private EntityManager entityManager;

    @Test
    void basePriceColumnsRenamedByAttributeOverrides() {
        Offer offer = new Offer();
        offer.setName("offer-2");
        offer.setBasePrice(new Money(new BigDecimal("12.50"), "USD"));
        entityManager.persist(offer);
        entityManager.flush();

        Object[] row = (Object[]) entityManager.createNativeQuery(
                        "SELECT base_amount, base_currency FROM offer WHERE id = :id")
                .setParameter("id", offer.getId()).getSingleResult();
        assertThat(((Number) row[0]).doubleValue()).isEqualTo(12.50);
        assertThat(row[1]).isEqualTo("USD");

        long renamed = (long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                                + "WHERE TABLE_NAME = 'OFFER' AND COLUMN_NAME IN ('BASE_AMOUNT', 'BASE_CURRENCY')",
                        Long.class).getSingleResult();
        assertThat(renamed).isEqualTo(2L);

        long defaultsLeft = (long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                                + "WHERE TABLE_NAME = 'OFFER' AND COLUMN_NAME IN ('AMOUNT', 'CURRENCY')",
                        Long.class).getSingleResult();
        assertThat(defaultsLeft).isZero();
    }
}
