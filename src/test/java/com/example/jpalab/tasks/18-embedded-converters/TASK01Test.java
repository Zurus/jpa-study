package com.example.jpalab.tasks.t18;

import com.example.jpalab.entity.Money;
import com.example.jpalab.entity.Offer;
import com.example.jpalab.entity.ProductStatus;
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
 * Тема 18, задание 01 (difficulty 1): сохраняем Offer с вложенным
 * @Embedded Money и читаем его обратно.
 * Метод-артефакт: OfferService.saveOffer(String, Money, ProductStatus).
 */
@DataJpaTest
@Import(OfferService.class)
class TASK01Test {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OfferService service;

    @Test
    void saveOfferRoundTripsEmbeddedPrice() {
        Offer saved = (Offer) TaskTestSupport.invoke(service, "saveOffer",
                new Class<?>[]{String.class, Money.class, ProductStatus.class},
                "offer-1", new Money(new BigDecimal("19.99"), "EUR"), ProductStatus.ACTIVE);
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();

        entityManager.clear();

        Offer found = entityManager.find(Offer.class, saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("offer-1");
        assertThat(found.getBasePrice()).isNotNull();
        assertThat(found.getBasePrice().getAmount()).isEqualByComparingTo("19.99");
        assertThat(found.getBasePrice().getCurrency()).isEqualTo("EUR");
        assertThat(found.getStatus()).isEqualTo(ProductStatus.ACTIVE);

        long rows = (long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM offer WHERE id = :id", Long.class)
                .setParameter("id", saved.getId()).getSingleResult();
        assertThat(rows).isEqualTo(1L);
    }
}
