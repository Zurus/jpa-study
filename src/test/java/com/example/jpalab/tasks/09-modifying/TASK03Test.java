package com.example.jpalab.tasks.t09;

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

/**
 * Тема 09, задание 03. Конвейер: bulk UPDATE → bulk DELETE (существующие методы).
 *
 * Два bulk-запроса из ProductRepository, уже объявленные в репозитории:
 * updateStockForExpensive(stock, minPrice) — «обнулить stock у дорогих»,
 * deleteOutOfStock() — «удалить всё с нулевым stock». Сценарий показывает,
 * как bulk-операции комбинируются: UPDATE готовит данные, DELETE их убирает.
 * Возвращаемые значения — число затронутых строк на каждом шаге.
 */
@DataJpaTest
class TASK03Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void updateThenDeletePipeline() {
        Category c = categoryRepository.saveAndFlush(Category.builder().name("Pipe").build());
        productRepository.save(Product.builder()
                .name("P1").price(new BigDecimal("10.00")).stockQuantity(1).category(c).build());
        productRepository.save(Product.builder()
                .name("P2").price(new BigDecimal("30.00")).stockQuantity(2).category(c).build());
        productRepository.save(Product.builder()
                .name("P3").price(new BigDecimal("40.00")).stockQuantity(3).category(c).build());
        em.flush();

        // шаг 1: bulk UPDATE — обнулить stock у товаров с ценой >= 25
        int zeroed = productRepository.updateStockForExpensive(0, new BigDecimal("25.00"));
        assertThat(zeroed).isEqualTo(2);

        // шаг 2: bulk DELETE — удалить товары с нулевым stock
        int deleted = productRepository.deleteOutOfStock();
        assertThat(deleted).isEqualTo(2);

        // итог: остался только «недорогой» товар с прежним stock
        List<Product> rest = productRepository.findAll();
        assertThat(rest).extracting(Product::getName).containsExactly("P1");
        assertThat(rest.get(0).getStockQuantity()).isEqualTo(1);
    }
}
