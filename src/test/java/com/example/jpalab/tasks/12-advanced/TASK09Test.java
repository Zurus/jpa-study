package com.example.jpalab.tasks.t12;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Тема 12, задание 09. Нюанс: bulk @Modifying UPDATE НЕ участвует в
 * оптимистичной блокировке — он не трогает колонку version.
 *
 * Пиновые свойства (не пересекаются ни с темой 09, ни с example-тестами):
 * 1) после bulk UPDATE stock меняется, а version остаётся 0;
 * 2) поэтому "устаревшая" копия (version = 0) успешно сохраняется —
 *    UPDATE ... WHERE version = 0 находит строку;
 * 3) JPA-обновление поднимает version 0 -> 1 и, так как пишет ВСЮ строку,
 *    перетирает bulk-значение stock старым (из копии).
 *
 * Bulk-запрос по stock — ВАШ метод репозитория
 * `int setStockForAll(int stock)` (@Modifying UPDATE) — до его реализации
 * тест красный.
 */
@SpringBootTest
class TASK09Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate tx;

    @BeforeEach
    void setUp() {
        tx = new TransactionTemplate(transactionManager);
    }

    private <T> T inTx(Supplier<T> work) {
        return tx.execute(status -> work.get());
    }

    private void inTx(Runnable work) {
        tx.executeWithoutResult(status -> work.run());
    }

    private Object[] rowById(Long id) {
        return inTx(() -> (Object[]) em.createNativeQuery(
                        "select price, stock_quantity, version from products where id = :id")
                .setParameter("id", id)
                .getSingleResult());
    }

    @AfterEach
    void cleanup() {
        tx.executeWithoutResult(status -> {
            productRepository.deleteAll();
            categoryRepository.deleteAll();
        });
    }

    @Test
    void bulkUpdateBypassesVersion() {
        Long id = inTx(() -> productRepository.save(Product.builder()
                .name("T12-09 Printer")
                .price(new BigDecimal("100.00"))
                .stockQuantity(4)
                .build()).getId());

        // "Читатель" забирает копию (version = 0, stock = 4, price = 100).
        Product stale = inTx(() -> em.find(Product.class, id));

        // Bulk UPDATE: stock 4 -> 7, version НЕ меняется (остается 0).
        int updated = inTx(() -> {
            em.flush();
            return TaskTestSupport.invoke(productRepository,
                    "setStockForAll", new Class<?>[]{int.class}, 7);
        });
        assertThat(updated).as("bulk UPDATE затронул строку").isEqualTo(1);
        Object[] afterBulk = rowById(id);
        assertThat(((Number) afterBulk[1]).intValue())
                .as("bulk UPDATE изменил stock").isEqualTo(7);
        assertThat(((Number) afterBulk[2]).longValue())
                .as("@Modifying не участвует в оптимистичной блокировке: version не поднят")
                .isEqualTo(0L);

        // "Устаревшая" копия (version = 0, stock = 4) сохраняет цену БЕЗ
        // конфликта: UPDATE ... WHERE version = 0 находит строку, т.к. bulk
        // не двигал version; полная строка пишет и старый stock = 4.
        assertThatCode(() -> inTx(() -> {
            stale.setPrice(new BigDecimal("120.00"));
            productRepository.save(stale);
        }))
                .as("обновление по version = 0 проходит успешно — конфликта нет")
                .doesNotThrowAnyException();

        Object[] finalRow = rowById(id);
        assertThat(new BigDecimal(finalRow[0].toString()))
                .as("JPA-обновление выполнилось и записало новую цену").isEqualByComparingTo("120.00");
        assertThat(((Number) finalRow[1]).intValue())
                .as("полная строка из устаревшей копии перетёрла bulk-stock").isEqualTo(4);
        assertThat(((Number) finalRow[2]).longValue())
                .as("именно JPA-UPDATE поднял version").isEqualTo(1L);
    }
}
