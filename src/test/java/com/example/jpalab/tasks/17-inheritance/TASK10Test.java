package com.example.jpalab.tasks.t17;

import com.example.jpalab.service.ProductHierarchyService;
import com.example.jpalab.tasks.TaskTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 17, задание 10. Сравнение стратегий по стоимости persist:
 * SINGLE_TABLE — 1 prepared statement; JOINED — 2 (корень + подкласс);
 * TABLE_PER_CLASS с SEQUENCE — 2 (next value + INSERT).
 * Метод-артефакт: ProductHierarchyService.persistStatementCounts() —
 * замеряет prepared statements при persist+flush новой сущности
 * каждой из трёх иерархий через Hibernate Statistics.
 * До реализации — красный (метод отсутствует).
 */
@DataJpaTest
@Import(ProductHierarchyService.class)
class TASK10Test {

    @Autowired
    private ProductHierarchyService service;

    @Test
    void persistStatementCountsPerStrategy() {
        Map<String, Integer> counts = TaskTestSupport.invoke(service, "persistStatementCounts",
                new Class<?>[]{});
        assertThat(counts).hasSize(3);
        assertThat(counts.get("singleTable")).isEqualTo(1);
        assertThat(counts.get("joined")).isEqualTo(2);
        assertThat(counts.get("tablePerClass")).isEqualTo(2);
    }
}
