package com.example.jpalab.tasks.t18;

import com.example.jpalab.entity.Money;
import com.example.jpalab.entity.Offer;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 18, задание 08 (difficulty 8): вложенный @Embeddable
 * com.example.jpalab.entity.PriceRange (min/max — Money) со
 * @AttributeOverrides ВНУТРИ embeddable-класса; Offer.priceRange —
 * @Embedded. Колонки: min_amount, min_currency, max_amount, max_currency.
 */
@DataJpaTest
class TASK08Test {

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
    void nestedEmbeddableWithInternalOverrides() {
        Object range = TaskTestSupport.newInstance("com.example.jpalab.entity.PriceRange");
        invokeSetter(range, "setMin", new Money(new BigDecimal("5.00"), "EUR"));
        invokeSetter(range, "setMax", new Money(new BigDecimal("50.00"), "EUR"));

        Offer offer = new Offer();
        offer.setName("offer-8");
        offer.setBasePrice(new Money(new BigDecimal("1.00"), "EUR"));
        invokeSetter(offer, "setPriceRange", range);
        entityManager.persist(offer);
        entityManager.flush();

        Object[] row = (Object[]) entityManager.createNativeQuery(
                        "SELECT min_amount, min_currency, max_amount, max_currency "
                                + "FROM offer WHERE id = :id")
                .setParameter("id", offer.getId()).getSingleResult();
        assertThat(((Number) row[0]).doubleValue()).isEqualTo(5.00);
        assertThat(row[1]).isEqualTo("EUR");
        assertThat(((Number) row[2]).doubleValue()).isEqualTo(50.00);
        assertThat(row[3]).isEqualTo("EUR");
    }
}
