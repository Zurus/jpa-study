package com.example.jpalab.tasks.t07;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.service.ProductService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 07, задание 10. N+1 в сервисной логике (реальная транзакция).
 *
 * «Сервисный» сценарий: загрузить список товаров и для каждого собрать
 * строку "<name> [<categoryName>]". Сначала наивно — ВАШИМ методом
 * `List<String> listProductsWithCategoryNames()` в `ProductService`
 * (findAll + цикл по getCategory — 1 + 5 SQL), затем через fetch graph —
 * ВАШИМ методом репозитория `List<Product> findAllWithCategoryGraph()`
 * (`@EntityGraph` на `category`, 1 SQL). Собранные строки совпадают —
 * выбор fetch-стратегии — вопрос производительности, а не корректности.
 *
 * Сценарий выполняется в реальных транзакциях
 * (@SpringBootTest + TransactionTemplate): своя настройка и очистка,
 * тест самодостаточен.
 */
@SpringBootTest
class TASK10Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductService productService;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TransactionTemplate tx;

    @TestConfiguration
    static class TestBeans {
        @Bean
        TransactionTemplate transactionTemplate(PlatformTransactionManager txm) {
            return new TransactionTemplate(txm);
        }
    }

    private <T> T inTx(Supplier<T> work) {
        return tx.execute(status -> work.get());
    }

    @AfterEach
    void cleanup() {
        inTx(() -> {
            productRepository.deleteAll();
            categoryRepository.deleteAll();
            return null;
        });
    }

    @Test
    void serviceScenarioNaiveVsFetch() {
        // setup: 5 товаров / 5 категорий (своя транзакция, commit)
        inTx(() -> {
            for (int i = 1; i <= 5; i++) {
                Category category = categoryRepository.save(
                        Category.builder().name("S7 C" + i).build());
                productRepository.save(Product.builder()
                        .name("S7 P" + i)
                        .price(BigDecimal.valueOf(i * 10L, 2))
                        .stockQuantity(1)
                        .category(category)
                        .build());
            }
            return null;
        });

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();

        // фаза «наивно»: ВАШ сервисный метод (как в задании 01) —
        // 1 SELECT по товарам + 5 по категориям
        List<String> naive = inTx(() -> {
            statistics.clear();
            return TaskTestSupport.invoke(productService,
                    "listProductsWithCategoryNames", new Class<?>[]{});
        });
        assertThat(naive).hasSize(5);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(6);

        // фаза «fetch graph»: ВАШ метод репозитория — 1 SELECT
        List<String> fetched = inTx(() -> {
            statistics.clear();
            List<Product> products = TaskTestSupport.invoke(productRepository,
                    "findAllWithCategoryGraph", new Class<?>[]{});
            List<String> rows = new ArrayList<>();
            for (Product product : products) {
                rows.add(product.getName() + " [" + product.getCategory().getName() + "]");
            }
            return rows;
        });
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        // результат сценария одинаков в обеих фазах
        assertThat(fetched).containsExactlyInAnyOrderElementsOf(naive);
    }
}
