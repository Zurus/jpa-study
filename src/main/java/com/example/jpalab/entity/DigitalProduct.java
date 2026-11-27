package com.example.jpalab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Тема 17 (наследование сущностей): цифровой товар (поле downloadUrl).
 *
 * В стартовом состоянии — самостоятельная сущность со своей таблицей
 * digital_product: полиморфная стратегия наследования ещё не настроена
 * (см. задания темы 17).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class DigitalProduct extends BaseEntity {

    @Column(name = "download_url")
    private String downloadUrl;
}
