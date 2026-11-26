package com.example.jpalab.tasks.t06;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.service.ProductLifecycleService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 06, задание 09. Каскад 25 товаров: N INSERT-ов без батчинга.
 *
 * cascade PERSIST по большой коллекции = N отдельных INSERT-ов:
 * без hibernate.jdbc.batch_size (&gt;1) и сортировки DML (order_inserts)
 * каждый каскадный INSERT — отдельный prepared statement (round-trip).
 * На больших коллекциях это узкое место; так это оптимизируется:
 * включается JDBC-батчинг (hibernate.jdbc.batch_size) и сортировка
 * DML (hibernate.order_inserts), тогда 25 INSERT-ов уходят батчем.
 * Тест батчинг НЕ включает — только измеряет дефолтное поведение.
 *
 * Граф «категория + 25 товаров» (с обратными ссылками) создаёт и сохраняет
 * ВАШ метод сервиса `Category createCategoryWithProducts(String categoryName,
 * int productCount)` в `ProductLifecycleService` — до его реализации
 * тест красный.
 */
@DataJpaTest
@Import(ProductLifecycleService.class)
class TASK09Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductLifecycleService lifecycleService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void cascadeInsertsCount() {
        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        // граф строит и сохраняет ВАШ метод
        Category category = TaskTestSupport.invoke(lifecycleService,
                "createCategoryWithProducts", new Class<?>[]{String.class, int.class},
                "BigCat", 25);
        assertThat(category.getId()).isNotNull();
        em.flush();

        // 1 insert into categories + 25 insert into products
        assertThat(statistics.getEntityInsertCount()).isEqualTo(26);
        // без батчинга каждый INSERT — отдельный prepared statement
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(26);

        // в БД: 25 строк товаров
        Long count = (Long) em.createNativeQuery("SELECT COUNT(*) FROM products", Long.class)
                .getSingleResult();
        assertThat(count).isEqualTo(25L);
    }
}
