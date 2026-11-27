package com.example.jpalab.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Тема 18 (embedded-типы и конвертеры): предложение по товару.
 *
 * Стартовое состояние: @Embedded Money basePrice без переопределения
 * колонок (колонки — amount, currency), status — явный EnumType.STRING,
 * discountType — без аннотаций маппинга. Задания темы 18 добавляют
 * переопределения колонок (@AttributeOverrides/@AttributeOverride),
 * новые вложенные типы и конвертеры (AttributeConverter/@Convert/@Converter).
 */
@Entity
@Table(name = "offer")
@Getter
@Setter
@NoArgsConstructor
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded
    private Money basePrice;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    private DiscountType discountType;
}
