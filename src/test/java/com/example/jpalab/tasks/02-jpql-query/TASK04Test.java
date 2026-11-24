package com.example.jpalab.tasks.t02;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static com.example.jpalab.tasks.TaskTestSupport.invoke;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 02, задание 04. Регистронезависимый поиск: UPPER/CONCAT/LIKE.
 *
 * Целевой метод (добавить в ProductRepository):
 * <pre>{@code
 * @Query("""
 *       SELECT p FROM Product p
 *       WHERE UPPER(p.name) LIKE UPPER(CONCAT('%', :fragment, '%'))
 *       ORDER BY p.name ASC
 *       """)
 * List<Product> searchByNameFragmentIgnoreCase(@Param("fragment") String fragment);
 * }</pre>
 * Фрагмент передаётся именованным параметром, «дички» собираются через CONCAT.
 */
@DataJpaTest
class TASK04Test {

    @Autowired
    private ProductRepository productRepository;

    private void save(String name, String price) {
        productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(1)
                .build());
    }

    @Test
    void upperConcatLikeIsCaseInsensitive() {
        // имена в смешанном регистре
        save("Super Widget", "10.00");
        save("super gizmo", "20.00");
        save("Boring item", "30.00");

        // «WIDG» (верхний регистр) находит «Super Widget»
        List<Product> byWidg = invoke(productRepository,
                "searchByNameFragmentIgnoreCase", new Class<?>[]{String.class}, "WIDG");
        assertThat(byWidg).extracting(Product::getName).containsExactly("Super Widget");

        // «super» находит «Super Widget» и «super gizmo»
        List<Product> bySuper = invoke(productRepository,
                "searchByNameFragmentIgnoreCase", new Class<?>[]{String.class}, "super");
        assertThat(bySuper).extracting(Product::getName)
                .containsExactlyInAnyOrder("Super Widget", "super gizmo");

        // одиночное «i»: фрагмент находится ВНУТРИ слова (LIKE без границ слов) —
        // все три имени содержат букву «i» в любом регистре
        List<Product> byLetterI = invoke(productRepository,
                "searchByNameFragmentIgnoreCase", new Class<?>[]{String.class}, "i");
        assertThat(byLetterI).hasSize(3);
        byLetterI.forEach(p ->
                assertThat(p.getName().toLowerCase()).contains("i"));

        // фрагмент, которого нет ни в одном имени, — пусто
        List<Product> missing = invoke(productRepository,
                "searchByNameFragmentIgnoreCase", new Class<?>[]{String.class}, "zzz");
        assertThat(missing).isEmpty();
    }
}
