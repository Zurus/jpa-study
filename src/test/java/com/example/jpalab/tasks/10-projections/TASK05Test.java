package com.example.jpalab.tasks.t10;

import com.example.jpalab.entity.Category;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.CategoryRepository;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 10, задание 05. Проекция с JOIN: товар + имя категории.
 *
 * Студент создаёт интерфейс-проекцию com.example.jpalab.dto.ProductCategoryView
 * (getter'ы getProductName, getCategoryName, getPrice) и метод в
 * ProductRepository: JPQL выбирает p.name, c.name, p.price через JOIN
 * p.category c с фильтром по имени категории. Тест не импортирует интерфейс
 * (его ещё нет) — элементы проверяются рефлексивно по именам getter'ов.
 */
@DataJpaTest
class TASK05Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void projectionWithJoinToCategory() {
        Category audio = categoryRepository.saveAndFlush(Category.builder().name("Audio").build());
        Category home = categoryRepository.saveAndFlush(Category.builder().name("Home").build());
        productRepository.save(Product.builder()
                .name("Headphones").price(new BigDecimal("300.00")).stockQuantity(1).category(audio).build());
        productRepository.save(Product.builder()
                .name("Mic").price(new BigDecimal("100.00")).stockQuantity(2).category(audio).build());
        productRepository.save(Product.builder()
                .name("Speaker").price(new BigDecimal("200.00")).stockQuantity(1).category(home).build());

        List<?> rows = TaskTestSupport.invoke(productRepository, "withCategoryName",
                new Class<?>[]{String.class}, "Audio");

        // товары другой категории (Speaker) не попадают
        assertThat(rows).hasSize(2);
        for (Object row : rows) {
            assertProjectedType(row, "com.example.jpalab.dto.ProductCategoryView");
        }

        // сортировка: цена desc — Headphones (300), Mic (100)
        assertThat(call(rows.get(0), "getProductName")).isEqualTo("Headphones");
        assertThat(call(rows.get(0), "getCategoryName")).isEqualTo("Audio");
        assertThat((BigDecimal) call(rows.get(0), "getPrice")).isEqualByComparingTo("300.00");
        assertThat(call(rows.get(1), "getProductName")).isEqualTo("Mic");
        assertThat(call(rows.get(1), "getCategoryName")).isEqualTo("Audio");
        assertThat((BigDecimal) call(rows.get(1), "getPrice")).isEqualByComparingTo("100.00");
    }

    private static Object call(Object target, String accessor) {
        try {
            return target.getClass().getMethod(accessor).invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("У элемента проекции нет публичного " + accessor
                    + "() — проверьте DTO (пакет com.example.jpalab.dto, см. задание)", e);
        }
    }

    private static void assertProjectedType(Object element, String dtoFqcn) {
        String canonical = element.getClass().getCanonicalName();
        if (canonical != null && canonical.equals(dtoFqcn)) {
            return;
        }
        for (Class<?> iface : element.getClass().getInterfaces()) {
            if (iface.getName().equals(dtoFqcn)) {
                return;
            }
        }
        throw new AssertionError("Элемент не имеет ожидаемого типа " + dtoFqcn
                + " (фактически: " + element.getClass().getName() + ")");
    }
}
