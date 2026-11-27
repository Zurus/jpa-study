package com.example.jpalab.tasks.t19;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.OrderRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 19, задание 10. Пакет операций, где каждая операция — собственная
 * транзакция (REQUIRES_NEW): сбой одной операции откатывает только её и не
 * трогает уже закоммиченные и последующие.
 *
 * Контракт задания (см. tasks/19-transactions/README.md):
 * сервис com.example.jpalab.service.ProductService (бин "productService"),
 * методы
 *   int processStockTransfers(Long fromId, Long toId, List<Integer> amounts, int maxToStock)
 *   void transferStockAtomically(Long fromId, Long toId, int amount, int maxToStock).
 * Поведение: processStockTransfers в транзакции прогоняет каждый amount
 * через transferStockAtomically, ловит сбои и продолжает; возвращает число
 * завершившихся переносов. transferStockAtomically: снять amount у
 * источника, прибавить получателю, flush; если склад получателя превысил
 * maxToStock — IllegalStateException (только этот перенос откатывается).
 *
 * Тест НЕ транзакционный: прямой вызов. До реализации тест красный: у
 * сервиса нет методов.
 */
@SpringBootTest
class TASK10Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

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

    private Object service() {
        return applicationContext.getBean("productService");
    }

    private Product seedProduct(String name, BigDecimal price, int stock) {
        return tx.execute(status -> {
            Product p = new Product();
            p.setName(name);
            p.setPrice(price);
            p.setStockQuantity(stock);
            return productRepository.save(p);
        });
    }

    private int stockOf(Long id) {
        return tx.execute(status -> (Integer) em.createNativeQuery(
                        "select stock_quantity from products where id = :id")
                .setParameter("id", id).getSingleResult());
    }

    @AfterEach
    void cleanup() {
        tx.executeWithoutResult(status -> {
            orderRepository.deleteAll();
            productRepository.deleteAll();
            categoryRepository.deleteAll();
        });
    }

    @Test
    void batchTransfersCommitIndependently() {
        Product from = seedProduct("T19-10 From", new BigDecimal("10.00"), 10);
        Product to = seedProduct("T19-10 To", new BigDecimal("20.00"), 5);

        // Операции: +2 (5->7, ок), +2 (7->9 > 8, сбой — только она откатывается),
        // +1 (7->8, ок).
        int ok = TaskTestSupport.invoke(service(), "processStockTransfers",
                new Class<?>[]{Long.class, Long.class, List.class, int.class},
                from.getId(), to.getId(), List.of(2, 2, 1), 8);

        assertThat(ok).as("две операции завершены, одна отклонена по вместимости").isEqualTo(2);
        assertThat(stockOf(from.getId())).as("применены только успешные переносы: 10-2-1").isEqualTo(7);
        assertThat(stockOf(to.getId())).as("проваленный перенос откатился независимо: 5+2+1").isEqualTo(8);
    }
}
