package com.example.jpalab.tasks.t12;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тема 12, задание 01. Bean Validation: ограничение @Size(max = 100)
 * на Product.name — его ДОБАВЛЯЕТ студент (см. tasks/12-advanced/TASK-01.md).
 *
 * До реализации тест красный: имя на 101 символ успешно попадает в БД
 * (колонока VARCHAR(255)), ConstraintViolationException не возникает.
 * После реализации: граница ровно 100 — 100 символов принимается,
 * 101 отклоняется на flush (commit) с ConstraintViolationException,
 * строка в БД не появляется.
 */
@SpringBootTest
class TASK01Test {

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

    private long countByName(String name) {
        return inTx(() -> (Long) em.createNativeQuery(
                        "select count(*) from products where name = :n")
                .setParameter("n", name)
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
    void tooLongNameRejectedByValidation() {
        String nameAtLimit = "A".repeat(100);
        String nameOverLimit = "B".repeat(101);

        // Граница инклюзивная: ровно 100 символов — принимается.
        Long idAtLimit = inTx(() -> productRepository.save(Product.builder()
                .name(nameAtLimit)
                .price(new BigDecimal("10.00"))
                .stockQuantity(1)
                .build()).getId());
        assertThat(idAtLimit).as("имя ровно в 100 символов проходит валидацию").isNotNull();
        assertThat(countByName(nameAtLimit)).isEqualTo(1);

        // 101 символ — нарушение @Size(max = 100): исключение на flush/commit,
        // транзакция откатилась, строки нет.
        assertThatThrownBy(() -> inTx(() -> productRepository.save(Product.builder()
                .name(nameOverLimit)
                .price(new BigDecimal("10.00"))
                .stockQuantity(1)
                .build())))
                .as("имя длиннее 100 символов отклоняется Bean Validation")
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("name");
        assertThat(countByName(nameOverLimit))
                .as("после отклонения строка в БД не появилась").isZero();
    }
}
