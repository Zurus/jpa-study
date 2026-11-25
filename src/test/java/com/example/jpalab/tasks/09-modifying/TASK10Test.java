package com.example.jpalab.tasks.t09;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 09, задание 10. Оба флага @Modifying в реальных транзакциях.
 *
 * Итоговый сценарий темы: bulk UPDATE с flushAutomatically = true И
 * clearAutomatically = true, реальные транзакции (@SpringBootTest +
 * TransactionTemplate). Перед вызовом метод в PC «грязный» (stock 8,
 * не flush-нут) — flushAutomatically должен сделать его видимым для
 * bulk-запроса; после вызова PC обязан очиститься (clearAutomatically),
 * чтобы немедленный findById вернул свежее значение, а не «старое»
 * из PC.
 */
@SpringBootTest
class TASK10Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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
    void bulkUpdateWithBothFlagsInRealTransaction() {
        // setup (своя транзакция, commit): категория A с P1 (stock 10) и P2 (stock 3)
        final Long[] ids = new Long[2];
        inTx(() -> {
            Category a = categoryRepository.save(Category.builder().name("A").build());
            Product p1 = productRepository.save(Product.builder()
                    .name("P1").price(new BigDecimal("10.00")).stockQuantity(10).category(a).build());
            Product p2 = productRepository.save(Product.builder()
                    .name("P2").price(new BigDecimal("20.00")).stockQuantity(3).category(a).build());
            ids[0] = p1.getId();
            ids[1] = p2.getId();
            return null;
        });

        // рабочая транзакция:
        // P1 «грязный» в PC (stock 8, не flush-нут) — без flushAutomatically
        // bulk UPDATE увидел бы 10 из БД;
        // после bulk UPDATE PC обязан очиститься — без clearAutomatically
        // немедленный findById вернул бы старую сущность (stock 8)
        inTx(() -> {
            Product p1 = productRepository.findById(ids[0]).orElseThrow();
            p1.setStockQuantity(8);

            Integer updated = TaskTestSupport.invoke(productRepository, "applyStockPenaltyInCategory",
                    new Class<?>[]{String.class, int.class}, "A", 5);
            // P1: 8 - 5 = 3 (8 >= 5, участвует); P2: 3 < 5 — не участвует
            assertThat(updated).isEqualTo(1);

            // em.clear() в тесте нет: clearAutomatically должен очистить PC —
            // немедленный find возвращает 3, а не старое 8
            Product fresh = productRepository.findById(ids[0]).orElseThrow();
            assertThat(fresh.getStockQuantity()).isEqualTo(3);
            return null;
        });

        // закоммиченное состояние (новая транзакция): P1 = 3, P2 = 3
        inTx(() -> {
            assertThat(productRepository.findById(ids[0]).orElseThrow().getStockQuantity())
                    .isEqualTo(3);
            assertThat(productRepository.findById(ids[1]).orElseThrow().getStockQuantity())
                    .isEqualTo(3);
            return null;
        });
    }
}
