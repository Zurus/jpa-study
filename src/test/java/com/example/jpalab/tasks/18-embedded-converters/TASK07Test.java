package com.example.jpalab.tasks.t18;

import com.example.jpalab.entity.Money;
import com.example.jpalab.entity.Offer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 18, задание 07 (difficulty 7): AttributeConverter складывает
 * Money в ОДНО текстовое поле колонки total (формат "сумма валюта"),
 * а не в две колонки как @Embedded.
 */
@DataJpaTest
class TASK07Test {

    @Autowired
    private EntityManager entityManager;

    private static void invokeSetter(Object target, String setterName, Object value) {
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                try {
                    method.invoke(target, value);
                    return;
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError("Не удалось вызвать " + setterName, e);
                }
            }
        }
        throw new AssertionError("Задание не реализовано: в "
                + target.getClass().getSimpleName() + " нет метода " + setterName);
    }

    @Test
    void converterStoresMoneyInSingleColumn() {
        Offer offer = new Offer();
        offer.setName("offer-7");
        offer.setBasePrice(new Money(new BigDecimal("1.00"), "EUR"));
        invokeSetter(offer, "setTotal", new Money(new BigDecimal("42.50"), "USD"));
        entityManager.persist(offer);
        entityManager.flush();

        Object total = entityManager.createNativeQuery(
                        "SELECT total FROM offer WHERE id = :id")
                .setParameter("id", offer.getId()).getSingleResult();
        assertThat(total).isEqualTo("42.50 USD");

        long columns = (long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                                + "WHERE TABLE_NAME = 'OFFER' AND COLUMN_NAME = 'TOTAL'",
                        Long.class).getSingleResult();
        assertThat(columns).isEqualTo(1L);
    }
}
