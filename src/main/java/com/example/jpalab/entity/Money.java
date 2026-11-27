package com.example.jpalab.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Тема 18 (embedded-типы и конвертеры): ценностный тип —
 * сумма с валютой. Не сущность: собственной таблицы нет,
 * хранится в таблице сущности, включающей его через @Embedded.
 * Колонки по умолчанию — amount и currency.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Money {

    private BigDecimal amount;

    private String currency;
}
