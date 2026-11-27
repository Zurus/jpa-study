package com.example.jpalab.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

/**
 * Тема 17 (наследование сущностей): иерархия BaseEntity →
 * DigitalProduct / PhysicalProduct и дополнительные иерархии
 * (vehicle — JOINED, document — TABLE_PER_CLASS).
 *
 * Студент добавляет методы из tasks/17-inheritance/TASK-XX.md.
 * Готовых методов в стартовом коде нет — только сервис и
 * инжектный EntityManager.
 */
@Service
public class ProductHierarchyService {

    @PersistenceContext
    private EntityManager entityManager;
}
