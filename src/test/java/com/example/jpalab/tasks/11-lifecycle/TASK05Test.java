package com.example.jpalab.tasks.t11;

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
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 11, задание 05. Dirty checking: изменение свойства MANAGED-
 * сущности без явного save/persist — Hibernate при flush (конец
 * транзакции) сам сравнит состояние и выполнит UPDATE, увеличив @Version.
 *
 * Контракт: сервис ProductLifecycleService, метод
 *   void updatePriceByDirtyChecking(Long id, BigDecimal newPrice)
 * (find в своей REQUIRES_NEW-транзакции, setPrice, БЕЗ явного save, commit).
 */
@SpringBootTest
class TASK05Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ApplicationContext applicationContext;

    private TransactionTemplate tx;

    @BeforeEach
    void setUp() {
        tx = new TransactionTemplate(transactionManager);
    }

    private <T> T inTx(Supplier<T> work) {
        return tx.execute(status -> work.get());
    }

    private Object lifecycleService() {
        try {
            return applicationContext.getBean("productLifecycleService");
        } catch (Exception e) {
            throw new AssertionError(
                    "Задание не реализовано: в контексте Spring нет бина \"productLifecycleService\"."
                            + " Убедитесь, что класс com.example.jpalab.service.ProductLifecycleService (@Service)"
                            + " существует, и добавьте в него метод по условию задания (см. tasks/11-lifecycle).", e);
        }
    }

    private Long createCommittedProduct(String name, String price, int stock) {
        Long[] ids = new Long[1];
        tx.executeWithoutResult(status -> ids[0] = productRepository.save(Product.builder()
                .name(name).price(new BigDecimal(price)).stockQuantity(stock).build()).getId());
        return ids[0];
    }

    private Object[] rowById(Long id) {
        return inTx(() -> (Object[]) em.createNativeQuery(
                        "select price, version, stock_quantity from products where id = :id")
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
    void dirtyCheckingUpdatesWithoutSave() {
        Long id = createCommittedProduct("T11-05 Printer", "300.00", 4);
        assertThat(rowById(id)[1]).as("version нового товара").isEqualTo(0L);

        BigDecimal newPrice = new BigDecimal("310.50");
        TaskTestSupport.invoke(lifecycleService(), "updatePriceByDirtyChecking",
                new Class<?>[]{Long.class, BigDecimal.class}, id, newPrice);

        Object[] row = rowById(id);
        assertThat((BigDecimal) row[0]).as("цена обновлена без явного save").isEqualByComparingTo(newPrice);
        assertThat(row[1]).as("version увеличен при flush (dirty checking)").isEqualTo(1L);
        assertThat(row[2]).as("остальные поля не тронуты").isEqualTo(4);
    }
}
