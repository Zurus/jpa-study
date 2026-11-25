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
 * Тема 11, задание 08. em.clear(): сбрасывает identity map ВНУТРИ одной
 * транзакции — повторный em.find по тому же id возвращает НОВУЮ инстанцию
 * (a != b), хотя данные и транзакция те же.
 *
 * Контракт: сервис ProductLifecycleService, метод
 *   Object[] identityMapAfterClear(Long id)
 * возвращает [differentInstances, sameId]
 * (в одной REQUIRES_NEW-транзакции: a = find; em.clear(); b = find).
 */
@SpringBootTest
class TASK08Test {

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

    @AfterEach
    void cleanup() {
        tx.executeWithoutResult(status -> {
            productRepository.deleteAll();
            categoryRepository.deleteAll();
        });
    }

    @Test
    void clearResetsIdentityMap() {
        Long id = createCommittedProduct("T11-08 Webcam", "140.00", 2);

        Object[] result = TaskTestSupport.invoke(lifecycleService(), "identityMapAfterClear",
                new Class<?>[]{Long.class}, id);

        assertThat((Boolean) result[0]).as("после em.clear() повторный find — новая инстанция").isTrue();
        assertThat((Boolean) result[1]).as("id у обеих инстанций совпадает").isTrue();
    }
}
