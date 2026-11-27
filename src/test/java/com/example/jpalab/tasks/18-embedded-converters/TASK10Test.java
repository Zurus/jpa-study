package com.example.jpalab.tasks.t18;

import com.example.jpalab.entity.Money;
import com.example.jpalab.entity.Offer;
import com.example.jpalab.service.OfferService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 18, задание 10 (difficulty 10): JPQL-агрегация по пути
 * вложенного типа (basePrice.amount) с фильтром по basePrice.currency;
 * пустое множество — SUM возвращает null.
 */
@DataJpaTest
@Import(OfferService.class)
class TASK10Test {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OfferService service;

    private void createOffer(String name, String currency, String amount) {
        Offer offer = new Offer();
        offer.setName(name);
        offer.setBasePrice(new Money(new BigDecimal(amount), currency));
        entityManager.persist(offer);
    }

    private BigDecimal sum(String currency) {
        return (BigDecimal) TaskTestSupport.invoke(service, "sumBaseAmountByCurrency",
                new Class<?>[]{String.class}, currency);
    }

    @Test
    void sumBaseAmountByCurrencyNullWhenEmpty() {
        createOffer("offer-10-1", "EUR", "10.00");
        createOffer("offer-10-2", "EUR", "25.50");
        createOffer("offer-10-3", "USD", "100.00");
        entityManager.flush();

        assertThat(sum("EUR")).isEqualByComparingTo("35.50");
        assertThat(sum("USD")).isEqualByComparingTo("100.00");
        assertThat(sum("GBP")).isNull();
    }
}
