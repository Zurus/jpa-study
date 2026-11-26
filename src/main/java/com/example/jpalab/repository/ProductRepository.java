package com.example.jpalab.repository;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Базовый репозиторий. Все специализированные методы (derived, JPQL, native,
 * bulk, проекции) студент ДОБАВЛЯЕТ сам по заданиям — готовые «примеры»
 * решений в стартовом коде отсутствуют.
 */
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /** Все товары категории (используется в ловушке задания 01/08: аргумент null). */
    List<Product> findByCategory(@Param("category") Category category);

    /**
     * Open interface projection (тема 10, задание 02): интерфейс уже создан,
     * метод-запрос студент добавляет сам.
     */
    interface ProductSummary {
        String getName();
        BigDecimal getPrice();
        int getStockQuantity();
    }
}
