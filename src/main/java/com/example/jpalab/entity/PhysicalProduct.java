package com.example.jpalab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Тема 17 (наследование сущностей): физический товар (поле weight).
 *
 * В стартовом состоянии — самостоятельная сущность со своей таблицей
 * physical_product: полиморфная стратегия наследования ещё не настроена
 * (см. задания темы 17).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class PhysicalProduct extends BaseEntity {

    @Column(name = "weight")
    private Double weight;
}
