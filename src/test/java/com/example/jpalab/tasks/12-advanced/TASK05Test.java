package com.example.jpalab.tasks.t12;

import com.example.jpalab.entity.Order;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.OrderRepository;
import com.example.jpalab.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 12, задание 05. Auditing: студент добавляет аудит в сущность Order —
 * поле `createdAt` (LocalDateTime, @CreatedDate) +
 * @EntityListeners(AuditingEntityListener.class) на классе.
 *
 * До реализации тест красный: у Order нет геттера getCreatedAt
 * (проверка через рефлексию — тест компилируется без изменений сущности).
 * После реализации: при INSERT аудитор заполняет поле, колонка created_at
 * в таблице orders существует и не NULL.
 */
@DataJpaTest
class TASK05Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void orderCreatedAtFilledByAuditing() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("T12-05 Phone")
                .price(new BigDecimal("500.00"))
                .stockQuantity(1)
                .build());
        Order order = orderRepository.save(Order.builder().product(product).build());

        Method getter = requireGetCreatedAt();
        Object value = getter.invoke(order);
        assertThat(value)
                .as("@CreatedDate createdAt заполнен фреймворком при INSERT")
                .isInstanceOf(LocalDateTime.class);
        assertThat((LocalDateTime) value)
                .as("значением служит время сохранения")
                .isAfter(LocalDateTime.now().minusMinutes(1))
                .isBefore(LocalDateTime.now().plusMinutes(1));

        Object raw = em.createNativeQuery(
                        "select created_at from orders where id = :id")
                .setParameter("id", order.getId())
                .getSingleResult();
        assertThat(raw).as("колонка created_at в БД существует и не NULL").isNotNull();
    }

    private Method requireGetCreatedAt() {
        try {
            return Order.class.getMethod("getCreatedAt");
        } catch (NoSuchMethodException e) {
            throw new AssertionError(
                    "Задание не реализовано: у сущности Order нет метода getCreatedAt(). "
                            + "Добавьте в src/main/java/com/example/jpalab/entity/Order.java "
                            + "поле `LocalDateTime createdAt` с аннотацией @CreatedDate "
                            + "и аннотацию @EntityListeners(AuditingEntityListener.class) на классе "
                            + "(см. tasks/12-advanced/TASK-05.md).", e);
        }
    }
}
