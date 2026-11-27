package com.example.jpalab.tasks.t18;

import com.example.jpalab.entity.Money;
import com.example.jpalab.entity.Offer;
import com.example.jpalab.entity.Product;
import com.example.jpalab.entity.ProductStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 18, задание 05 (difficulty 5): AttributeConverter для
 * ProductStatus (нижнее регистр) применяется ТОЛЬКО к Offer.status
 * через явный @Convert; Product.status остаётся в маппинге
 * EnumType.STRING (в колонке — DISCONTINUED, а не discontinued).
 */
@DataJpaTest
class TASK05Test {

    @Autowired
    private EntityManager entityManager;

    @Test
    void explicitConvertAppliesOnlyToAnnotatedField() {
        Offer offer = new Offer();
        offer.setName("offer-5");
        offer.setBasePrice(new Money(new BigDecimal("5.00"), "EUR"));
        offer.setStatus(ProductStatus.INACTIVE);
        entityManager.persist(offer);

        Product product = new Product();
        product.setName("P-5");
        product.setStatus(ProductStatus.DISCONTINUED);
        entityManager.persist(product);
        entityManager.flush();

        Object offerStatus = entityManager.createNativeQuery(
                        "SELECT status FROM offer WHERE id = :id")
                .setParameter("id", offer.getId()).getSingleResult();
        assertThat(offerStatus).isEqualTo("inactive");

        Object productStatus = entityManager.createNativeQuery(
                        "SELECT status FROM products WHERE id = :id")
                .setParameter("id", product.getId()).getSingleResult();
        assertThat(productStatus).isEqualTo("DISCONTINUED");
    }
}
