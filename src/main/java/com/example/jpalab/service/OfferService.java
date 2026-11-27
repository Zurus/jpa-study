package com.example.jpalab.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

/**
 * Тема 18 (embedded-типы и конвертеры): сущность Offer с
 * @Embedded Money и enum-полями.
 *
 * Студент добавляет методы из tasks/18-embedded-converters/TASK-XX.md.
 * Готовых методов в стартовом коде нет — только сервис и
 * инжектный EntityManager.
 */
@Service
public class OfferService {

    @PersistenceContext
    private EntityManager entityManager;
}
