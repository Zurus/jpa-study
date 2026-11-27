package com.example.jpalab.entity;

/**
 * Тема 18 (embedded-типы и конвертеры): статус товара.
 * В стартовом коде без аннотаций конвертирования —
 * маппинг enum в колонку настраивают задания темы 18.
 */
public enum ProductStatus {
    ACTIVE,
    INACTIVE,
    DISCONTINUED
}
