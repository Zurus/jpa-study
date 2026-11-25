package com.example.jpalab.tasks.t09;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тема 09, задание 09. Ловушка: bulk DELETE обходит JPA cascade.
 *
 * Bulk DELETE (DELETE FROM ... WHERE) не загружает сущности: cascade,
 * orphanRemoval, @Version и lifecycle-колбэки НЕ выполняются — удаляется
 * только строка. Если на категорию ссылаются товары, БД бросит нарушение FK.
 * Правильный порядок — сначала «много» (товары), потом «один» (категория).
 * (Путь через em.remove / repository delete cascade уважает — тема 06.)
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
    void bulkDeleteBypassesCascade() {
        Category a = categoryRepository.saveAndFlush(Category.builder().name("A").build());
        productRepository.save(Product.builder()
                .name("P1").price(new BigDecimal("10.00")).stockQuantity(1).category(a).build());
        productRepository.save(Product.builder()
                .name("P2").price(new BigDecimal("20.00")).stockQuantity(1).category(a).build());
        Category b = categoryRepository.saveAndFlush(Category.builder().name("B").build());
        productRepository.save(Product.builder()
                .name("P3").price(new BigDecimal("30.00")).stockQuantity(1).category(b).build());
        em.flush();

        // правильный порядок: сначала «много» (товары), потом «один» (категория)
        Integer deletedProducts = TaskTestSupport.invoke(productRepository, "deleteProductsInCategory",
                new Class<?>[]{String.class}, "B");
        assertThat(deletedProducts).isEqualTo(1);

        Integer deletedCategories = TaskTestSupport.invoke(categoryRepository, "deleteCategoriesByName",
                new Class<?>[]{String.class}, "B");
        assertThat(deletedCategories).isEqualTo(1);

        Long bCount = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM categories WHERE name = ?", Long.class)
                .setParameter(1, "B").getSingleResult();
        assertThat(bCount).isZero();
        Long p3Count = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM products WHERE name = ?", Long.class)
                .setParameter(1, "P3").getSingleResult();
        assertThat(p3Count).isZero();

        // ловушка: bulk DELETE категории с живыми товарами — нарушение FK,
        // bulk SQL cascade/orphanRemoval не выполняет
        assertThatThrownBy(() -> TaskTestSupport.invoke(categoryRepository, "deleteCategoriesByName",
                new Class<?>[]{String.class}, "A"))
                .isInstanceOf(DataIntegrityViolationException.class);

        // после неудачной операции сбрасываем persistence context
        em.clear();

        // категория A осталась в БД
        Long aCount = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM categories WHERE name = ?", Long.class)
                .setParameter(1, "A").getSingleResult();
        assertThat(aCount).isEqualTo(1L);
    }
}
