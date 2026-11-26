package com.example.jpalab.tasks.t02;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тема 02, задание 08. Ловушка: свойство сущности vs колонка БД.
 *
 * Динамические запросы через EntityManager показывают ловушку:
 * (а) JPQL с именем КОЛОНКИ  p.stock_quantity  -> ошибка;
 * (б) JPQL с именем СВОЙСТВА p.stockQuantity   -> работает;
 * (в) ваш метод `findStockAtLeast(int)` в `ProductRepository` (JPQL @Query
 *     с именем СВОЙСТВА) должен вернуть тот же результат, что и (б).
 *
 * JPQL — язык модели сущностей: snake_case-колонки существуют только в DDL
 * и native-запросах, Hibernate подставит их сам.
 *
 * До реализации метода тест красный (TaskTestSupport не найдёт findStockAtLeast).
 */
@DataJpaTest
class TASK08Test {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void columnVsProperty() {
        // 2 товара: stock 5 и 15
        productRepository.save(Product.builder()
                .name("Low").price(new BigDecimal("10.00")).stockQuantity(5).build());
        productRepository.save(Product.builder()
                .name("High").price(new BigDecimal("20.00")).stockQuantity(15).build());

        // (а) имя колонки в JPQL — путь не распознан, запрос невалиден
        assertThatThrownBy(() -> em.createQuery(
                "SELECT p FROM Product p WHERE p.stock_quantity > :s", Product.class)
                .setParameter("s", 10)
                .getResultList())
                .isInstanceOf(IllegalArgumentException.class);

        // (б) имя свойства — ожидаемые товары
        List<Product> found = em.createQuery(
                "SELECT p FROM Product p WHERE p.stockQuantity > :s", Product.class)
                .setParameter("s", 10)
                .getResultList();
        assertThat(found).extracting(Product::getName).containsExactly("High");

        // (в) ваш метод в репозитории: JPQL по имени СВОЙСТВА (см. задание)
        List<Product> viaMethod = TaskTestSupport.invoke(productRepository, "findStockAtLeast",
                new Class<?>[]{int.class}, 10);
        assertThat(viaMethod).as("метод findStockAtLeast использует имя свойства, а не колонки")
                .extracting(Product::getName).containsExactly("High");
    }
}
