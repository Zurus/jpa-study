package com.example.jpalab.tasks.t08;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 08, задание 08. Нестабильный порядок: тай-брейк по уникальному ключу.
 *
 * При сортировке только по цене СУБД НЕ ОБЯЗАНА сохранять порядок равных
 * элементов: при повторных запросах (и особенно при вставках/удалениях
 * между страницами) элемент с равным ключом может «перескочить» на соседнюю
 * страницу (дубликат) или, наоборот, потеряться. Правило: последним ключом
 * сортировки должен быть уникальный (id) — тогда разбиение на страницы
 * детерминировано.
 */
@DataJpaTest
class TASK08Test {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void uniqueTieBreakerMakesPagingDeterministic() {
        // цены 10, 10, 20, 20 — две «неразличимые» пары
        Product i1 = productRepository.saveAndFlush(Product.builder()
                .name("T1").price(new BigDecimal("10.00")).stockQuantity(1).build());
        Product i2 = productRepository.saveAndFlush(Product.builder()
                .name("T2").price(new BigDecimal("10.00")).stockQuantity(1).build());
        Product i3 = productRepository.saveAndFlush(Product.builder()
                .name("T3").price(new BigDecimal("20.00")).stockQuantity(1).build());
        Product i4 = productRepository.saveAndFlush(Product.builder()
                .name("T4").price(new BigDecimal("20.00")).stockQuantity(1).build());

        // (а) только по цене: разбиение пар НЕ гарантировано.
        // Проверяем только безопасные свойства: размеры страниц и покрытие
        // всеми 4 id без дублей (в этом запуске, без изменений данных).
        Sort priceOnly = Sort.by("price");
        Page<Product> a0 = productRepository.findAll(PageRequest.of(0, 2, priceOnly));
        Page<Product> a1 = productRepository.findAll(PageRequest.of(1, 2, priceOnly));
        assertThat(a0.getContent()).hasSize(2);
        assertThat(a1.getContent()).hasSize(2);
        List<Long> aAll = new ArrayList<>();
        a0.getContent().forEach(p -> aAll.add(p.getId()));
        a1.getContent().forEach(p -> aAll.add(p.getId()));
        assertThat(aAll).containsExactlyInAnyOrder(i1.getId(), i2.getId(), i3.getId(), i4.getId());

        // (б) цена + id: ТОЧНО детерминированное разбиение
        Sort priceThenId = Sort.by("price").and(Sort.by("id"));
        Page<Product> b0 = productRepository.findAll(PageRequest.of(0, 2, priceThenId));
        assertThat(b0.getContent()).extracting(Product::getId)
                .containsExactly(i1.getId(), i2.getId());
        Page<Product> b1 = productRepository.findAll(PageRequest.of(1, 2, priceThenId));
        assertThat(b1.getContent()).extracting(Product::getId)
                .containsExactly(i3.getId(), i4.getId());

        // детерминизм: повторный запрос той же страницы — тот же состав
        Page<Product> b0Again = productRepository.findAll(PageRequest.of(0, 2, priceThenId));
        assertThat(b0Again.getContent()).extracting(Product::getId)
                .containsExactly(i1.getId(), i2.getId());
    }
}
