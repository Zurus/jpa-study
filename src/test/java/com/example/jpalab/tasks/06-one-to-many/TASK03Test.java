package com.example.jpalab.tasks.t06;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 06, задание 03. orphanRemoval: удаление из коллекции.
 *
 * orphanRemoval = true превращает «выпадение из коллекции» в удаление
 * сущности (неявно влечёт cascade REMOVE): достаточно remove managed-
 * элемента из коллекции managed-родителя, em.remove не понадобился —
 * в БД уходит delete from products where id = ?.
 */
@DataJpaTest
class TASK03Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void orphanRemoval() {
        // категория с 2 товарами
        Category category = Category.builder().name("OrphanCat").build();
        Product keep = Product.builder().name("Keep").price(new BigDecimal("1.00")).stockQuantity(1).build();
        Product removeMe = Product.builder().name("RemoveMe").price(new BigDecimal("2.00")).stockQuantity(1).build();
        category.getProducts().add(keep);
        keep.setCategory(category);
        category.getProducts().add(removeMe);
        removeMe.setCategory(category);
        categoryRepository.saveAndFlush(category);
        em.clear();

        // managed-родитель; удаляем managed-элемент ТОЛЬКО из коллекции
        Category managed = categoryRepository.findById(category.getId()).orElseThrow();
        Product managedRemoveMe = managed.getProducts().stream()
                .filter(p -> "RemoveMe".equals(p.getName()))
                .findFirst()
                .orElseThrow();
        managed.getProducts().remove(managedRemoveMe);
        // em.remove НЕ вызывается

        Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
        statistics.clear();

        em.flush();

        // ровно один delete по товару
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
        assertThat(statistics.getEntityDeleteCount()).isEqualTo(1);

        // строка удалённого товара исчезла, второй на месте
        Long byRemoved = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM products WHERE id = ?", Long.class)
                .setParameter(1, managedRemoveMe.getId())
                .getSingleResult();
        assertThat(byRemoved).isZero();
        Long inCategory = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM products WHERE category_id = ?", Long.class)
                .setParameter(1, category.getId())
                .getSingleResult();
        assertThat(inCategory).isEqualTo(1L);
    }
}
