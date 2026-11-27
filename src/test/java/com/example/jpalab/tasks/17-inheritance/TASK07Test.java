package com.example.jpalab.tasks.t17;

import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 17, задание 07. Стратегия JOINED на отдельной иерархии:
 * корень — таблица vehicles (общие поля + дискриминатор vtype),
 * каждый подкласс — своя таблица (cars, buses) с FK на корень.
 * При persist записывается в ДВЕ таблицы (2 prepared statements).
 * Артефакт — классы студента com.example.jpalab.entity.vehicle.*
 * (Vehicle / Car / Bus, имена и поля — в TASK-07.md).
 * До реализации — красный (классы отсутствуют).
 */
@DataJpaTest
class TASK07Test {

    @Autowired
    private EntityManager entityManager;

    private long count(String sql) {
        return (Long) entityManager.createNativeQuery(sql).getSingleResult();
    }

    @Test
    void joinedHierarchyBaseTablePlusSubclassTables() throws Exception {
        Object car = TaskTestSupport.newInstance("com.example.jpalab.entity.vehicle.Car");
        invokeSetter(car, "setSeats", 4);
        Object bus = TaskTestSupport.newInstance("com.example.jpalab.entity.vehicle.Bus");
        invokeSetter(bus, "setCapacity", 50);

        Statistics statistics = entityManager.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();
        entityManager.persist(car);
        entityManager.flush();
        // JOINED: запись идёт в две таблицы — корень и подкласс
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2L);

        assertThat(count("SELECT COUNT(*) FROM vehicles")).isEqualTo(1L);
        assertThat(count("SELECT COUNT(*) FROM cars WHERE seats = 4")).isEqualTo(1L);
        assertThat(count("SELECT COUNT(*) FROM vehicles v JOIN cars c ON c.id = v.id WHERE c.seats = 4"))
                .isEqualTo(1L);
        assertThat(count("SELECT COUNT(*) FROM vehicles WHERE vtype IS NOT NULL")).isEqualTo(1L);

        entityManager.persist(bus);
        entityManager.flush();
        assertThat(count("SELECT COUNT(*) FROM vehicles")).isEqualTo(2L);
        assertThat(count("SELECT COUNT(*) FROM buses WHERE capacity = 50")).isEqualTo(1L);

        // полиморфный запрос по корню видит оба подкласса
        List<?> all = entityManager.createQuery("select v from Vehicle v").getResultList();
        assertThat(all).hasSize(2);

        entityManager.clear();
        Class<?> vehicleClass = Class.forName("com.example.jpalab.entity.vehicle.Vehicle");
        Class<?> carClass = Class.forName("com.example.jpalab.entity.vehicle.Car");
        Object byRoot = entityManager.find(vehicleClass, idOf(car));
        assertThat(byRoot).isInstanceOf(carClass);
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

    private static Long idOf(Object target) {
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals("getId") && method.getParameterCount() == 0) {
                try {
                    return (Long) method.invoke(target);
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError("Не удалось вызвать getId", e);
                }
            }
        }
        throw new AssertionError("Задание не реализовано: нет метода getId()");
    }
}
