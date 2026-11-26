package com.example.jpalab.tasks.t09;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 09, задание 08. Ловушка: «устаревшая» сущность переписывает bulk UPDATE.
 *
 * ВАШ метод репозитория `int raiseAllPricesBy(BigDecimal delta)` —
 * bulk UPDATE БЕЗ clearAutomatically (и без flushAutomatically):
 * после него PC не инвалидируется и продолжает держать старое состояние.
 * Если затем «невинно» поправить поле у этой устаревшей managed-сущности
 * и сделать em.flush(), Hibernate сгенерит UPDATE по всей строке
 * (@DynamicUpdate не стоит) — со СТАРЫМ значением цены, и результат
 * bulk-запроса будет «перезаписан». До его реализации тест красный.
 */
@DataJpaTest
class TASK08Test {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void staleEntityOverwritesBulkUpdate() {
        Product a = productRepository.save(Product.builder()
                .name("A").price(new BigDecimal("100.00")).stockQuantity(1).build());
        Product b = productRepository.save(Product.builder()
                .name("B").price(new BigDecimal("200.00")).stockQuantity(1).build());
        em.flush();

        // A загружен в PC: в памяти price = 100.00
        Product loadedA = productRepository.findById(a.getId()).orElseThrow();

        // bulk UPDATE без флагов: в БД A = 200.00, B = 300.00,
        // а в PC остался старый A (100.00) — bulk-запрос PC не трогает
        int updated = TaskTestSupport.invoke(productRepository,
                "raiseAllPricesBy", new Class<?>[]{BigDecimal.class},
                new BigDecimal("100.00"));
        assertThat(updated).isEqualTo(2);

        // результат bulk UPDATE в БД (native — мимо PC): A = 200.00
        BigDecimal dbA = (BigDecimal) em.createNativeQuery(
                "SELECT price FROM products WHERE id = ?", BigDecimal.class)
                .setParameter(1, a.getId()).getSingleResult();
        assertThat(dbA).isEqualByComparingTo("200.00");

        // «невинно» меняем поле у устаревшей сущности и flush-им:
        // Hibernate пишет ВСЮ строку из состояния PC — цена 100.00
        // перетрёт bulk-результат 200.00
        loadedA.setStockQuantity(5);
        em.flush();

        BigDecimal clobbered = (BigDecimal) em.createNativeQuery(
                "SELECT price FROM products WHERE id = ?", BigDecimal.class)
                .setParameter(1, a.getId()).getSingleResult();
        assertThat(clobbered).isEqualByComparingTo("100.00");

        Integer stock = (Integer) em.createNativeQuery(
                "SELECT stock_quantity FROM products WHERE id = ?", Integer.class)
                .setParameter(1, a.getId()).getSingleResult();
        assertThat(stock).isEqualTo(5);

        // B не был в «затронутом» UPDATE — не пострадал
        BigDecimal dbB = (BigDecimal) em.createNativeQuery(
                "SELECT price FROM products WHERE id = ?", BigDecimal.class)
                .setParameter(1, b.getId()).getSingleResult();
        assertThat(dbB).isEqualByComparingTo("300.00");
    }
}
