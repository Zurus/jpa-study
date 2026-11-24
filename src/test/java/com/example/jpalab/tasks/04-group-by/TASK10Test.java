package com.example.jpalab.tasks.t04;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 04, задание 10. Page по сгруппированному запросу.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("""
 *       SELECT c.name, COUNT(p.id) FROM Category c
 *       LEFT JOIN c.products p
 *       GROUP BY c.name ORDER BY c.name ASC
 *       """)
 * Page<Object[]> categoryCountPage(Pageable pageable);
 * }</pre>
 * Spring Data для GROUP BY-запросов строит count как подзапрос вокруг всего
 * сгруппированного SELECT: totalElements — число ГРУПП, а не строк-товаров.
 */
@DataJpaTest
class TASK10Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Page<Object[]> call(Pageable pageable) {
        return invoke(productRepository, "categoryCountPage",
                new Class<?>[]{Pageable.class}, pageable);
    }

    @Test
    void pageOverGroups() {
        // 3 категории: "A" x2, "B" x0, "C" x0
        Category a = categoryRepository.saveAndFlush(Category.builder().name("A").build());
        categoryRepository.saveAndFlush(Category.builder().name("B").build());
        categoryRepository.saveAndFlush(Category.builder().name("C").build());
        productRepository.save(Product.builder()
                .name("A1").price(new BigDecimal("10.00")).stockQuantity(1)
                .category(a).build());
        productRepository.save(Product.builder()
                .name("A2").price(new BigDecimal("10.00")).stockQuantity(1)
                .category(a).build());

        Page<Object[]> page0 = call(PageRequest.of(0, 2));
        // totalElements == 3 — число ГРУПП (категорий), а не строк-товаров
        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page0.hasNext()).isTrue();
        // страница 0 — 2 группы
        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getContent().get(0)[0]).isEqualTo("A");
        assertThat(page0.getContent().get(0)[1]).isEqualTo(2L);
        assertThat(page0.getContent().get(1)[0]).isEqualTo("B");
        assertThat(page0.getContent().get(1)[1]).isEqualTo(0L);

        Page<Object[]> page1 = call(PageRequest.of(1, 2));
        // страница 1 — 1 группа, hasNext == false
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page1.getContent().get(0)[0]).isEqualTo("C");
        assertThat(page1.getContent().get(0)[1]).isEqualTo(0L);
        assertThat(page1.hasNext()).isFalse();
    }
}
