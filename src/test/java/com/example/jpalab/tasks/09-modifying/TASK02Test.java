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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 09, задание 02. Bulk DELETE по условию.
 *
 * DELETE FROM ... WHERE в @Modifying-методу удаляет строки в БД напрямую
 * (без em.remove, без lifecycle-колбэков и cascade). Возвращает число
 * удалённых строк.
 */
@DataJpaTest
class TASK02Test {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void bulkDeleteByCondition() {
        // три товара без категории (поле category допускает null)
        productRepository.save(Product.builder()
                .name("X").price(new BigDecimal("10.00")).stockQuantity(1).build());
        productRepository.save(Product.builder()
                .name("Y").price(new BigDecimal("20.00")).stockQuantity(1).build());
        productRepository.save(Product.builder()
                .name("Z").price(new BigDecimal("30.00")).stockQuantity(1).build());
        em.flush();

        Integer deleted = TaskTestSupport.invoke(productRepository, "deleteProductsByName",
                new Class<?>[]{String.class}, "Y");
        assertThat(deleted).isEqualTo(1);

        // в БД остались X и Z
        List<String> names = em.createNativeQuery(
                "SELECT name FROM products ORDER BY name", String.class).getResultList();
        assertThat(names).containsExactly("X", "Z");
    }
}
