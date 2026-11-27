package com.example.jpalab.tasks.t18;

import com.example.jpalab.entity.DiscountType;
import com.example.jpalab.entity.Money;
import com.example.jpalab.entity.Offer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 18, задание 06 (difficulty 6): @Converter(autoApply = true) —
 * конвертер DiscountType (PERCENT→"P", FIXED→"F") применяется ко всем
 * полям этого типа без аннотации на самом поле.
 */
@DataJpaTest
class TASK06Test {

    @Autowired
    private EntityManager entityManager;

    @Test
    void autoAppliedConverterNeedsNoAnnotation() {
        Offer percent = new Offer();
        percent.setName("offer-6-percent");
        percent.setBasePrice(new Money(new BigDecimal("1.00"), "EUR"));
        percent.setDiscountType(DiscountType.PERCENT);
        entityManager.persist(percent);

        Offer fixed = new Offer();
        fixed.setName("offer-6-fixed");
        fixed.setBasePrice(new Money(new BigDecimal("2.00"), "EUR"));
        fixed.setDiscountType(DiscountType.FIXED);
        entityManager.persist(fixed);
        entityManager.flush();

        Object percentValue = entityManager.createNativeQuery(
                        "SELECT discount_type FROM offer WHERE id = :id")
                .setParameter("id", percent.getId()).getSingleResult();
        Object fixedValue = entityManager.createNativeQuery(
                        "SELECT discount_type FROM offer WHERE id = :id")
                .setParameter("id", fixed.getId()).getSingleResult();

        assertThat(percentValue).isEqualTo("P");
        assertThat(fixedValue).isEqualTo("F");
    }
}
