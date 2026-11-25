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
 * Тема 10, задание 07. LEFT JOIN в проекции: товары БЕЗ категории.
 *
 * INNER JOIN p.category тихо «съел бы» товары без категории. LEFT JOIN
 * сохраняет их, а имя категории в проекции равно null. Студент создаёт
 * интерфейс com.example.jpalab.dto.ProductNameCategory (getProductName,
 * getCategoryName) и метод без параметров в ProductRepository.
 */
@DataJpaTest
class TASK07Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void leftJoinProjectionKeepsNullCategory() {
        Category c = categoryRepository.saveAndFlush(Category.builder().name("WithCat").build());
        productRepository.save(Product.builder()
                .name("P1").price(new BigDecimal("10.00")).stockQuantity(1).category(c).build());
        productRepository.save(Product.builder()
                .name("P2").price(new BigDecimal("20.00")).stockQuantity(1).category(c).build());
        productRepository.save(Product.builder()
                .name("P3").price(new BigDecimal("30.00")).stockQuantity(1).build());

        List<?> rows = TaskTestSupport.invoke(productRepository, "namesWithOptionalCategory",
                new Class<?>[]{});

        // 3 товара, включая P3 без категории (INNER JOIN дал бы 2 строки)
        assertThat(rows).hasSize(3);
        for (Object row : rows) {
            assertProjectedType(row, "com.example.jpalab.dto.ProductNameCategory");
        }

        // сортировка по имени: P1, P2, P3
        assertThat(call(rows.get(0), "getProductName")).isEqualTo("P1");
        assertThat(call(rows.get(0), "getCategoryName")).isEqualTo("WithCat");
        assertThat(call(rows.get(1), "getProductName")).isEqualTo("P2");
        assertThat(call(rows.get(1), "getCategoryName")).isEqualTo("WithCat");
        assertThat(call(rows.get(2), "getProductName")).isEqualTo("P3");
        assertThat(call(rows.get(2), "getCategoryName")).isNull();
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
