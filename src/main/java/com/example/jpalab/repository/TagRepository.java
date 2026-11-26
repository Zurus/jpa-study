package com.example.jpalab.repository;

import com.example.jpalab.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Тема 16 (@ManyToMany): пустой репозиторий сущности Tag.
 * Методы-артефакты студента здесь НЕ объявляются — они live в
 * `com.example.jpalab.service.ProductTagService`.
 */
public interface TagRepository extends JpaRepository<Tag, Long> {
}
