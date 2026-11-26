package com.example.jpalab.tasks.t07;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.service.ProductService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 07, задание 01. Измерить N+1 на @ManyToOne (LAZY).
 *
 * Схема «1+N»: одна SELECT по таблице сущностей (products) + по одной
 * SELECT на каждую LAZY-ассоциацию (category), которую сервис «дёргает»
 * в цикле. Без em.clear() после создания данных N+1 не видно —
 * persistence context уже «знает» все объекты.
 *
 * Наивный сервисный сценарий (findAll + цикл по getCategory().getName())
 * реализует ВАШ метод `List<String> listProductsWithCategoryNames()` в
 * `ProductService` (строки вида "<name> [<categoryName>]", по всем товарам)
 * — до его реализации тест красный.
 */
@DataJpaTest
@Import(ProductService.class)
class TASK01Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductService productService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void measureNPlus1OnManyToOne() {
        // 5 товаров, 5 РАЗНЫХ категорий
        for (int i = 1; i <= 5; i++) {
            Category category = categoryRepository.save(
                    Category.builder().name("N1 C" + i).build());
            Product product = Product.builder()
                    .name("N1 P" + i)
                    .price(BigDecimal.valueOf(i * 10L, 2))
                    .stockQuantity(1)
                    .category(category)
                    .build();
            productRepository.save(product);
        }
        em.flush();
        em.clear();

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        // ВАШ «наивный» сервисный метод: 1 SELECT по products
        // + 5 SELECT по категориям (LAZY-инициализация в цикле)
        List<String> rows = TaskTestSupport.invoke(productService,
                "listProductsWithCategoryNames", new Class<?>[]{});
        assertThat(rows).containsExactlyInAnyOrder(
                "N1 P1 [N1 C1]", "N1 P2 [N1 C2]", "N1 P3 [N1 C3]",
                "N1 P4 [N1 C4]", "N1 P5 [N1 C5]");

        // итого 1 + N = 6
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(6);
    }
}
