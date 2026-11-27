package com.example.jpalab.repository;

import com.example.jpalab.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Тема 18 (embedded-типы и конвертеры): репозиторий Offer (пустой —
 * методы добавляет студент, если нужны).
 */
public interface OfferRepository extends JpaRepository<Offer, Long> {
}
