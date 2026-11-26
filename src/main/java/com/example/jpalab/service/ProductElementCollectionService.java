package com.example.jpalab.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

/**
 * Тема 15 (@ElementCollection): коллекции простых значений.
 * Студент добавляет методы, работающие с `Product.tags` (Set&lt;String&gt;)
 * и `Product.attributes` (Map&lt;String, String&gt;) через EntityManager.
 *
 * Готовых методов в стартовом коде нет — только сервис и
 * инжектный EntityManager.
 */
@Service
public class ProductElementCollectionService {

    @PersistenceContext
    private EntityManager entityManager;
}
