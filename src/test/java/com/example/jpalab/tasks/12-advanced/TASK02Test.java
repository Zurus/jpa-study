package com.example.jpalab.tasks.t12;

import com.example.jpalab.entity.Category;
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

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тема 12, задание 02. Bean Validation: ограничение @Size(max = 50)
 * на Category.name — его ДОБАВЛЯЕТ студент (см. tasks/12-advanced/TASK-02.md).
 *
 * До реализации тест красный: имя на 51 символ успешно сохраняется.
 * После реализации: 50 символов принимается, 51 отклоняется
 * ConstraintViolationException, строка не появляется.
 */
@SpringBootTest
class TASK02Test {

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

    private long countByName(String name) {
        return inTx(() -> (Long) em.createNativeQuery(
                        "select count(*) from categories where name = :n")
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
    void tooLongCategoryNameRejectedByValidation() {
        String nameAtLimit = "C".repeat(50);
        String nameOverLimit = "D".repeat(51);

        // Граница инклюзивная: ровно 50 символов — принимается.
        Long idAtLimit = inTx(() -> categoryRepository.save(
                Category.builder().name(nameAtLimit).build()).getId());
        assertThat(idAtLimit).as("имя ровно в 50 символов проходит валидацию").isNotNull();
        assertThat(countByName(nameAtLimit)).isEqualTo(1);

        // 51 символ — нарушение @Size(max = 50).
        assertThatThrownBy(() -> inTx(() -> categoryRepository.save(
                Category.builder().name(nameOverLimit).build())))
                .as("имя категории длиннее 50 символов отклоняется Bean Validation")
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("name");
        assertThat(countByName(nameOverLimit))
                .as("после отклонения строка в БД не появилась").isZero();
    }
}
