package com.example.jpalab.tasks.t07;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.jpalab.tasks.TaskTestSupport;

/**
 * Тема 07, задание 09. Ловушка: count-запрос с JOIN FETCH и
 * «мультипликация» строк.
 *
 * (а) `count(c)` + `JOIN FETCH c.products` — Hibernate запрещает fetch
 *     ассоциации в агрегат-запросе (бросает IllegalArgumentException);
 * (б) обычный `JOIN c.products` в count «мультиплицирует» строки:
 *     считается число joined-строк (6), а не число категорий (4);
 * (в) ваш метод `CategoryRepository.findAllWithProductsFetched()`
 *     (@EntityGraph по коллекции products) — один запрос на все категории
 *     ВКЛЮЧАЯ пустую, без дубликатов инстанции и без дополнительных
 *     запросов к коллекциям.
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

        // (в) ВАШ метод: одна загрузка (категории + коллекции) без дублей
        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();
        @SuppressWarnings("unchecked")
        List<Category> all = TaskTestSupport.invoke(categoryRepository,
                "findAllWithProductsFetched", new Class<?>[]{});
        // ровно ОДИН запрос на категории с коллекциями
        assertThat(statistics.getPrepareStatementCount())
                .as("@EntityGraph/fetch join должен дать один запрос")
                .isEqualTo(1);
        // все 4 категории, включая пустую (LEFT JOIN графа)
        assertThat(all).extracting(Category::getName)
                .containsExactlyInAnyOrder("F A", "F B", "F C", "F D");
        long uniqueIdentities = all.stream()
                .map(System::identityHashCode)
                .distinct()
                .count();
        assertThat(uniqueIdentities).isEqualTo(all.size());
        // коллекции уже загружены: size() без доп. SQL
        statistics.clear();
        int totalProducts = all.stream()
                .mapToInt(c -> c.getProducts().size())
                .sum();
        assertThat(totalProducts).isEqualTo(6);
        assertThat(statistics.getPrepareStatementCount())
                .as("коллекции уже инициализированы графом")
                .isZero();
    }
}
