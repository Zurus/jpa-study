package com.example.jpalab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Тема 17 (наследование сущностей): базовый класс иерархии «товары».
 *
 * Поля id и createdAt объявлены один раз и наследуются
 * DigitalProduct и PhysicalProduct. Таблицы у самого BaseEntity нет:
 * это @MappedSuperclass, а не @Entity (см. задания темы 17).
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
