package com.example.jpalab.tasks.t05;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.service.ProductLifecycleService;
import com.example.jpalab.tasks.TaskTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тема 05, задание 08. Нельзя удалить «одного», пока на него ссылаются.
 *
 * FK — это правило БД: строка, на которую указывают другие строки,
 * не удаляется. Порядок удаления — сначала «много» (товары),
 * потом «один» (категория) — либо каскад со стороны коллекции (тема 06).
 *
 * «Правильный порядок» выполняет ВАШ метод сервиса
 * `void deleteCategoryWithProducts(Long categoryId)` в
 * `ProductLifecycleService` (сначала удаляет товары категории, затем её
 * саму, в одной транзакции) — до его реализации тест красный.
 */
@DataJpaTest
@Import(ProductLifecycleService.class)
class TASK08Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductLifecycleService lifecycleService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void cannotDeleteReferencedCategory() {
        Category category = categoryRepository.saveAndFlush(
                Category.builder().name("Busy").build());
        productRepository.save(Product.builder()
                .name("P1").price(new BigDecimal("10.00")).stockQuantity(1)
                .category(category).build());
        productRepository.save(Product.builder()
                .name("P2").price(new BigDecimal("20.00")).stockQuantity(1)
                .category(category).build());
        em.flush();

        // удаление категории при живых товарах — нарушение FK
        assertThatThrownBy(() -> {
            categoryRepository.delete(category);
            em.flush();
        }).isInstanceOf(ConstraintViolationException.class);

        // сбрасываем persistence context: после неудачного flush повторный
        // auto-flush перед запросом повторно бросает то же исключение
        em.clear();

        // категория осталась в БД
        Object fk = em.createNativeQuery(
                "SELECT COUNT(*) FROM categories WHERE id = :id", Long.class)
                .setParameter("id", category.getId())
                .getSingleResult();
        assertThat(fk).isEqualTo(1L);

        // правильный порядок — ВАШИМ методом (одна транзакция сервиса)
        em.clear();
        TaskTestSupport.invoke(lifecycleService, "deleteCategoryWithProducts",
                new Class<?>[]{Long.class}, category.getId());

        // категория и товары удалены
        Long remainingCategories = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM categories WHERE id = :id", Long.class)
                .setParameter("id", category.getId())
                .getSingleResult();
        assertThat(remainingCategories).isZero();
        Long remainingProducts = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM products", Long.class)
                .getSingleResult();
        assertThat(remainingProducts).isZero();
    }
}
