package com.example.jpalab.tasks.t17;

import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 17, задание 08. Стратегия TABLE_PER_CLASS на отдельной
 * иерархии: у каждого подкласса — Своя таблица со ВСЕМИ колонками
 * (включая унаследованные), общей корневой таблицы с данными нет;
 * полиморфный запрос по корню — один UNION-запрос.
 * Артефакт — классы студента com.example.jpalab.entity.document.*
 * (Document / PdfDocument / OdtDocument, имена и поля — в TASK-08.md).
 * До реализации — красный (классы отсутствуют).
 */
@DataJpaTest
class TASK08Test {

    @Autowired
    private EntityManager entityManager;

    private long count(String sql) {
        return (Long) entityManager.createNativeQuery(sql).getSingleResult();
    }

    private long columnCount(String table, String column) {
        return (Long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS"
                                + " WHERE TABLE_NAME = :t AND COLUMN_NAME = :c")
                .setParameter("t", table).setParameter("c", column).getSingleResult();
    }

    @Test
    void tablePerClassSeparateTablesFullColumns() {
        Object pdf = TaskTestSupport.newInstance("com.example.jpalab.entity.document.PdfDocument");
        invokeSetter(pdf, "setTitle", "Report");
        invokeSetter(pdf, "setPageCount", 12);
        invokeSetter(pdf, "setCreatedAt", LocalDateTime.of(2024, 4, 1, 10, 0));
        Object odt = TaskTestSupport.newInstance("com.example.jpalab.entity.document.OdtDocument");
        invokeSetter(odt, "setTitle", "Notes");
        invokeSetter(odt, "setTemplate", "plain");
        entityManager.persist(pdf);
        entityManager.persist(odt);
        entityManager.flush();

        // данные — в отдельных таблицах подклассов
        assertThat(count("SELECT COUNT(*) FROM pdf_documents"
                + " WHERE title = 'Report' AND page_count = 12")).isEqualTo(1L);
        assertThat(count("SELECT COUNT(*) FROM odt_documents"
                + " WHERE title = 'Notes' AND template = 'plain'")).isEqualTo(1L);

        // в каждой таблице есть унаследованные колонки корня
        assertThat(count("SELECT COUNT(*) FROM pdf_documents WHERE created_at IS NOT NULL")).isEqualTo(1L);
        assertThat(columnCount("PDF_DOCUMENTS", "TITLE")).isEqualTo(1L);
        assertThat(columnCount("ODT_DOCUMENTS", "CREATED_AT")).isEqualTo(1L);
        // ...а чужих колонок нет
        assertThat(columnCount("PDF_DOCUMENTS", "TEMPLATE")).isZero();
        assertThat(columnCount("ODT_DOCUMENTS", "PAGE_COUNT")).isZero();

        // полиморфный запрос по корню — ровно один UNION-запрос
        Statistics statistics = entityManager.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();
        List<?> all = entityManager.createQuery("select d from Document d").getResultList();
        assertThat(all).hasSize(2);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1L);
    }

    private static void invokeSetter(Object target, String name, Object value) {
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == 1) {
                try {
                    method.invoke(target, value);
                    return;
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError("Не удалось вызвать " + name, e);
                }
            }
        }
        throw new AssertionError("Задание не реализовано: у "
                + target.getClass().getName() + " нет сеттера " + name);
    }
}
