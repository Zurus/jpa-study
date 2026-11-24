package com.example.jpalab.tasks.t07;

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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тема 07, задание 09. Ловушка: count-запрос с JOIN FETCH и
 * «мультипликация» строк.
 *
 * (а) `count(c)` + `JOIN FETCH c.products` — Hibernate запрещает fetch
 *     ассоциации в агрегат-запросе (бросает IllegalArgumentException);
 * (б) обычный `JOIN c.products` в count «мультиплицирует» строки:
 *     считается число joined-строк (6), а не число категорий (4);
 * (в) лимитированный fetch-join-запрос не содержит дубликатов инстанции —
 *     Hibernate дедуплицирует сущности в результате.
 *
 * Запросы динамические (EntityManager): битый fetch join в @Query убил
 * бы запуск приложения.
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
    void fetchJoinCountTrap() {
        // данные: "F A" (3 товара), "F B" (2), "F C" (1), "F D" (0)
        int[] sizes = {3, 2, 1, 0};
        String[] names = {"F A", "F B", "F C", "F D"};
        for (int i = 0; i < sizes.length; i++) {
            Category category = categoryRepository.save(
                    Category.builder().name(names[i]).build());
            for (int j = 0; j < sizes[i]; j++) {
                Product product = Product.builder()
                        .name(names[i] + " P" + j)
                        .price(BigDecimal.valueOf(i * 10L + j, 2))
                        .stockQuantity(1)
                        .category(category)
                        .build();
                productRepository.save(product);
            }
        }
        em.flush();
        em.clear();

        // (а) count + JOIN FETCH — запрещено
        assertThatThrownBy(() -> em.createQuery(
                "SELECT count(c) FROM Category c JOIN FETCH c.products", Long.class)
                .getSingleResult())
                .isInstanceOf(IllegalArgumentException.class);

        // (б) plain JOIN в count считает joined-строки, а не категории
        Long withJoin = (Long) em.createQuery(
                "SELECT count(c) FROM Category c JOIN c.products", Long.class)
                .getSingleResult();
        assertThat(withJoin).isEqualTo(6L); // 3 + 2 + 1 (+ 0)
        Long plain = (Long) em.createQuery(
                "SELECT count(c) FROM Category c", Long.class)
                .getSingleResult();
        assertThat(plain).isEqualTo(4L);

        // (в) лимитированный fetch-join: инстанции в результате уникальны
        @SuppressWarnings("unchecked")
        List<Category> limited = (List<Category>) em
                .createQuery("SELECT c FROM Category c JOIN FETCH c.products", Category.class)
                .setMaxResults(3)
                .getResultList();
        assertThat(limited).isNotEmpty();
        long uniqueIdentities = limited.stream()
                .map(System::identityHashCode)
                .distinct()
                .count();
        assertThat(uniqueIdentities).isEqualTo(limited.size());
        // внутренний join: категории без товаров ("F D") в результате не участвуют
        assertThat(limited).allSatisfy(category ->
                assertThat(category.getName()).isIn("F A", "F B", "F C"));
    }
}
