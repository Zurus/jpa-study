package com.example.jpalab.tasks.t06;

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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 06, задание 08. «Двойное владение»: managed-товар в коллекции новой категории.
 *
 * «Владеть» товаром может только одна категория: FK — единственная
 * истина, а коллекция — лишь её отражение. Добавив managed-товар в
 * коллекцию новой категории C без setCategory(C), мы получим:
 * FK по-прежнему A; сохранение C не сломалось (merge managed-товара —
 * no-op по FK); после refresh(c) коллекция C окажется пуста.
 */
@DataJpaTest
class TASK08Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void doubleOwnership() {
        // P сохранён в A
        Category a = categoryRepository.saveAndFlush(Category.builder().name("TrueOwner").build());
        Product product = Product.builder()
                .name("DoubleOwned")
                .price(new BigDecimal("3.00"))
                .stockQuantity(1)
                .category(a)
                .build();
        productRepository.saveAndFlush(product);
        em.clear();

        // managed-товар (из persistence context)
        Category managedA = categoryRepository.findById(a.getId()).orElseThrow();
        Product managedProduct = (Product) managedA.getProducts().get(0);

        // НОВАЯ категория C: добавляем managed-товар в её коллекцию,
        // P.setCategory НЕ вызываем
        Category c = Category.builder().name("NewClaimant").build();
        c.getProducts().add(managedProduct);
        categoryRepository.save(c);
        em.flush();

        // FK товара остался A
        Object fk = em.createNativeQuery(
                "SELECT category_id FROM products WHERE id = ?", Long.class)
                .setParameter(1, product.getId())
                .getSingleResult();
        assertThat(fk).isEqualTo(a.getId());

        // коллекция C в памяти «думает», что товар в ней...
        assertThat(c.getProducts()).hasSize(1);
        // ...но после refresh — пусто: по FK товар C не принадлежит
        em.refresh(c);
        assertThat(c.getProducts()).isEmpty();
    }
}
