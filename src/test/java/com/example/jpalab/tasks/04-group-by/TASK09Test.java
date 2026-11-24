package com.example.jpalab.tasks.t04;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
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
 * Тема 04, задание 09. Ловушка: алиас агрегата в HAVING.
 *
 * Динамические JPQL-запросы через EntityManager:
 * (а) HAVING ссылающийся на АЛИАС агрегата («HAVING total > :min», где total —
 *     псевдоним для COUNT(p.id)) — ошибка уже при создании запроса
 *     (алиасы в HAVING разрешает не всякий SQL-диалект, например MySQL — да,
 *     стандарт/H2 — нет);
 * (б) портативный вариант: повторное выражение агрегата в HAVING.
 */
@DataJpaTest
class TASK09Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void havingAlias() {
        // данные: "Many" x2, "Empty" x0
        Category many = categoryRepository.saveAndFlush(
                Category.builder().name("Many").build());
        categoryRepository.saveAndFlush(Category.builder().name("Empty").build());
        productRepository.save(Product.builder()
                .name("M1").price(new BigDecimal("10.00")).stockQuantity(1)
                .category(many).build());
        productRepository.save(Product.builder()
                .name("M2").price(new BigDecimal("10.00")).stockQuantity(1)
                .category(many).build());

        // (а) алиас total в HAVING — исключение при создании запроса
        assertThatThrownBy(() -> em.createQuery("""
                SELECT c.name, COUNT(p.id) total FROM Category c
                LEFT JOIN c.products p
                GROUP BY c.name HAVING total > :min
                """))
                .isInstanceOf(IllegalArgumentException.class);

        // (б) повторное выражение агрегата — работает,
        //     пустая группа отфильтрована
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createQuery("""
                SELECT c.name, COUNT(p.id) FROM Category c
                LEFT JOIN c.products p
                GROUP BY c.name HAVING COUNT(p.id) > :min
                """, Object[].class)
                .setParameter("min", 0L)
                .getResultList();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)[0]).isEqualTo("Many");
        assertThat(rows.get(0)[1]).isEqualTo(2L);
    }
}
