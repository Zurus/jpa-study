package com.example.jpalab.repository;

import com.example.jpalab.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ==== Тема 4: GROUP BY и HAVING через JPQL ====

    /**
     * Статистика по категориям: имя, количество товаров, сумма цен.
     * LEFT JOIN сохраняет категории без товаров (count = 0, sum = null).
     * Возвращает Object[]: [String name, Long count, BigDecimal sumOrNull].
     */
    @Query("""
            SELECT c.name, COUNT(p.id), SUM(p.price)
            FROM Category c
            LEFT JOIN c.products p
            GROUP BY c.name
            ORDER BY c.name ASC
            """)
    List<Object[]> productStatsByCategory();

    /** HAVING: только группы, где товаров не меньше заданного. */
    @Query("""
            SELECT c.name
            FROM Category c
            JOIN c.products p
            GROUP BY c.name
            HAVING COUNT(p.id) >= :minProducts
            ORDER BY c.name ASC
            """)
    List<String> categoryNamesWithAtLeastProducts(@Param("minProducts") int minProducts);
}
