package com.example.jpalab.tasks.t05;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 05, задание 10. Откат транзакции: FK возвращается.
 *
 * Требует РЕАЛЬНЫХ транзакций (@DataJpaTest оборачивает весь тест в одну
 * транзакцию, где откатить «внутри» нельзя): @SpringBootTest +
 * TransactionTemplate. Dirty checking и UPDATE — часть транзакции:
 * пока транзакция не закоммичена, «обновлённый» FK живёт только
 * в БД-транзакции, и rollback его стирает.
 *
 * Тест самодостаточен: своя настройка в отдельной транзакции (commit),
 * откатываемая транзакция с изменением FK, проверка в новых транзакциях,
 * очистка БД в @AfterEach.
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

    @AfterEach
    void cleanup() {
        tx.executeWithoutResult(status -> {
            productRepository.deleteAll();
            categoryRepository.deleteAll();
        });
    }

    private Long categoryColumn(Long productId) {
        return tx.execute(status -> (Long) em.createNativeQuery(
                "SELECT category_id FROM products WHERE id = :id", Long.class)
                .setParameter("id", productId)
                .getSingleResult());
    }

    @Test
    void rollbackRestoresFk() {
        // setup (отдельная транзакция, commit): A, B, товар в A
        Object[] ids = tx.execute(status -> {
            Category a = categoryRepository.save(Category.builder().name("A").build());
            Category b = categoryRepository.save(Category.builder().name("B").build());
            Product product = productRepository.save(Product.builder()
                    .name("Rollback")
                    .price(new BigDecimal("10.00"))
                    .stockQuantity(1)
                    .category(a)
                    .build());
            return new Object[]{a.getId(), b.getId(), product.getId()};
        });
        Long aId = (Long) ids[0];
        Long bId = (Long) ids[1];
        Long productId = (Long) ids[2];

        // откатываемая транзакция: перевод товара в B + flush, БЕЗ commit
        try {
            tx.executeWithoutResult(status -> {
                Product product = em.find(Product.class, productId);
                product.setCategory(em.find(Category.class, bId));
                em.flush();
                // принудительный откат транзакции
                throw new RuntimeException("rollback for the test");
            });
        } catch (RuntimeException expected) {
            // откат выполнен
        }

        // в новой транзакции: FK в БД снова id категории A (UPDATE не зафиксирован)
        assertThat(categoryColumn(productId)).isEqualTo(aId);

        // свежая загрузка товара: категория A
        String categoryName = tx.execute(status ->
                em.find(Product.class, productId).getCategory().getName());
        assertThat(categoryName).isEqualTo("A");
    }
}
