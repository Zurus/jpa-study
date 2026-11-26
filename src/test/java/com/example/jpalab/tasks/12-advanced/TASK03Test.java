package com.example.jpalab.tasks.t12;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тема 12, задание 03. Bean Validation: валидация происходит на FLUSH,
 * а не на setter и не на persist.
 *
 * Наблюдение над уже существующими ограничениями Product
 * (@NotBlank на name).
 * Пиновые свойства (не пересекаются с AdvancedEntityExampleTest):
 * 1) setter не бросает исключение даже для managed-сущности;
 * 2) исключение возникает именно на em.flush;
 * 3) UPDATE не выполнялся: имя/цена/версия в БД не изменились.
 *
 * Обновление имени выполняет ВАШ метод
 * `void updateProductName(Long id, String name)` в
 * `ProductVersioningService` (em.find + setName + flush в своей транзакции)
 * — до его реализации тест красный.
 */
@SpringBootTest
class TASK03Test {

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

    private Object versioningService() {
        try {
            return applicationContext.getBean("productVersioningService");
        } catch (Exception e) {
            throw new AssertionError(
                    "Задание не реализовано: в контексте Spring нет бина \"productVersioningService\". "
                            + "Создайте класс com.example.jpalab.service.ProductVersioningService (@Service) "
                            + "и реализуйте в нём метод updateProductName(Long, String) "
                            + "по условию задания (см. tasks/12-advanced/README.md).", e);
        }
    }

    private void inTx(Runnable work) {
        tx.executeWithoutResult(status -> work.run());
    }

    private Object[] rowById(Long id) {
        return inTx(() -> (Object[]) em.createNativeQuery(
                        "select name, price, version from products where id = :id")
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
    void validationFiresOnFlushNotOnSetter() {
        String name = "T12-03 Widget";
        Long id = inTx(() -> productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal("100.00"))
                .stockQuantity(1)
                .build()).getId());

        // 1) Setter детэчед-копии: никаких исключений.
        Product detached = inTx(() -> em.find(Product.class, id));
        assertThatCode(() -> detached.setName("   "))
                .as("setter не выполняет валидацию")
                .doesNotThrowAnyException();

        // 2) Managed-копия: и setter, и сам момент после setter — тишина;
        //    нарушение всплывает только на flush (flush внутри ВАШЕГО метода).
        assertThatThrownBy(() -> TaskTestSupport.invoke(versioningService(),
                "updateProductName", new Class<?>[]{Long.class, String.class}, id, "   "))
                .as("валидация dirty-сущности выполняется на flush")
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("name");

        // 3) UPDATE не выполнялся: строка осталась в исходном виде,
        //    version не поднят.
        Object[] row = rowById(id);
        assertThat(row[0]).as("имя в БД не изменилось").isEqualTo(name);
        assertThat(new BigDecimal(row[1].toString())).as("цена не изменилась").isEqualByComparingTo("100.00");
        assertThat(((Number) row[2]).longValue())
                .as("без выполненного UPDATE version остаётся 0").isEqualTo(0L);
    }
}
