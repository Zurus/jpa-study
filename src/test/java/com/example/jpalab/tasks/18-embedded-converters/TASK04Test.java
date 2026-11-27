package com.example.jpalab.tasks.t18;

import com.example.jpalab.entity.Money;
import com.example.jpalab.entity.Offer;
import com.example.jpalab.entity.ProductStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 18, задание 04 (difficulty 4): @Enumerated(EnumType.ORDINAL) —
 * в колонку last_known_status пишутся номера констант (0/1/2),
 * а не их имена.
 */
@DataJpaTest
class TASK04Test {

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
    void ordinalEnumStoresNumbers() {
        List<ProductStatus> statuses = new ArrayList<>(List.of(
                ProductStatus.ACTIVE, ProductStatus.INACTIVE, ProductStatus.DISCONTINUED));
        List<Object> stored = new ArrayList<>();

        for (int i = 0; i < statuses.size(); i++) {
            Offer offer = new Offer();
            offer.setName("offer-4-" + i);
            offer.setBasePrice(new Money(new BigDecimal("1.00"), "EUR"));
            invokeSetter(offer, "setLastKnownStatus", statuses.get(i));
            entityManager.persist(offer);
            entityManager.flush();

            Object value = entityManager.createNativeQuery(
                            "SELECT last_known_status FROM offer WHERE id = :id")
                    .setParameter("id", offer.getId()).getSingleResult();
            stored.add(value);
        }

        assertThat(((Number) stored.get(0)).intValue()).isEqualTo(0);
        assertThat(((Number) stored.get(1)).intValue()).isEqualTo(1);
        assertThat(((Number) stored.get(2)).intValue()).isEqualTo(2);
    }
}
