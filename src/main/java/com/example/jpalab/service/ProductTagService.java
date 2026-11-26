package com.example.jpalab.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

/**
 * Тема 16 (@ManyToMany): связь Product <-> Tag.
 * Студент добавляет методы, работающие с владельской коллекцией
 * `Product.productTags` (Set&lt;Tag&gt;), обратной стороной `Tag.products`
 * (mappedBy) и таблицей связей product_tag_link.
 *
 * Готовых методов в стартовом коде нет — только сервис и
 * инжектный EntityManager.
 */
@Service
public class ProductTagService {

    @PersistenceContext
    private EntityManager entityManager;
}
