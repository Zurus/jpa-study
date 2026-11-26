package com.example.jpalab.tasks.t02;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 02, задание 10. L1-кэш: запрос и изменённая managed-сущность.
 *
 * Запрос «товар по id» выполняется ВАШЕМ методом репозитория
 * `findJpqlById(Long id)` (JPQL @Query) — до его реализации тест красный.
 * Он доказывает семантику первого уровня кэша:
 * - сущность из JPQL управляема;
 * - изменение её в памяти без flush делает этот же объект видимым
 *   в результате ПОВТОРНОГО запроса, хотя SQL-запрос выполняется заново;
 * - после em.clear() из БД загружается НОВЫЙ экземпляр (значение из БД,
 *   т.к. flush не было).
 *
 * «Повторный запрос вернул старые данные из БД и новые данные из памяти
 * одновременно» — следствие работы L1-кэша (persistence context).
 */
@DataJpaTest
class TASK10Test {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    private List<Product> byId(Long id) {
        return TaskTestSupport.invoke(productRepository, "findJpqlById",
                new Class<?>[]{Long.class}, id);
    }

    @Test
    void firstLevelCacheReturnsManagedInstance() {
        // 1. сохраняем товар (цена 100)
        Product saved = productRepository.saveAndFlush(Product.builder()
                .name("Cached")
                .price(new BigDecimal("100.00"))
                .stockQuantity(1)
                .build());

        // auto-flush выключен: повторные запросы не должны прогонять
        // dirty-сущность через UPDATE, иначе SELECT-счётчики исказятся
        em.setFlushMode(FlushModeType.COMMIT);

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();

        // 2. JPQL -> instance A
        statistics.clear();
        Product a = byId(saved.getId()).get(0);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1); // один SELECT

        // 3. изменение в памяти БЕЗ flush
        a.setPrice(new BigDecimal("999.00"));

        // 4. тот же JPQL ещё раз -> instance B
        statistics.clear();
        Product b = byId(saved.getId()).get(0);
        // ВТОРОЙ SELECT всё же выполнен (L1-кэш кэширует сущности, а не SQL)
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
        // B — тот же самый объект, что и A
        assertThat(b).isSameAs(a);
        // цена у B — 999: из памяти, а не из БД
        assertThat(b.getPrice()).isEqualByComparingTo("999.00");

        // 5. после em.clear() -> instance C
        em.clear();
        statistics.clear();
        Product c = byId(saved.getId()).get(0);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
        // C — новый экземпляр (старый выпал из persistence context)
        assertThat(c).isNotSameAs(a);
        // цена 100: значение из БД, т.к. flush не было
        assertThat(c.getPrice()).isEqualByComparingTo("100.00");
    }
}
