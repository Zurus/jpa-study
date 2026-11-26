package com.example.jpalab.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

/**
 * Тема 14 (Criteria API): программные запросы. Студент добавляет методы,
 * которые строят запрос через CriteriaBuilder/CriteriaQuery поверх
 * EntityManager и возвращают результат.
 *
 * Готовых запросов в стартовом коде нет — только сервис и
 * инжектный EntityManager.
 */
@Service
public class ProductCriteriaService {

    @PersistenceContext
    private EntityManager entityManager;
}
