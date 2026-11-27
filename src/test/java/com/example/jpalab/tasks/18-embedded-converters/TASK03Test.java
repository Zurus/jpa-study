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
 * Тема 18, задание 03 (difficulty 3): второй @Embedded Money
 * (discountedPrice) получает собственные колонки discount_amount /
 * discount_currency через @AttributeOverrides — иначе два вложенных
 * типа одного класса напишут в одни и те же колонки.
 */
@DataJpaTest
class TASK03Test {

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
    void secondEmbeddedMoneyUsesOwnColumns() {
        Offer offer = new Offer();
        offer.setName("offer-3");
        offer.setBasePrice(new Money(new BigDecimal("10.00"), "EUR"));
        invokeSetter(offer, "setDiscountedPrice", new Money(new BigDecimal("7.25"), "EUR"));
        entityManager.persist(offer);
        entityManager.flush();

        Object[] row = (Object[]) entityManager.createNativeQuery(
                        "SELECT base_amount, base_currency, discount_amount, discount_currency "
                                + "FROM offer WHERE id = :id")
                .setParameter("id", offer.getId()).getSingleResult();
        assertThat(((Number) row[0]).doubleValue()).isEqualTo(10.00);
        assertThat(row[1]).isEqualTo("EUR");
        assertThat(((Number) row[2]).doubleValue()).isEqualTo(7.25);
        assertThat(row[3]).isEqualTo("EUR");
    }
}
