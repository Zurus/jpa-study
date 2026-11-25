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
 * Тема 10, задание 09. Проекция с JOIN: фильтр по связанной сущности +
 * диапазон цен + детерминированная сортировка, 4 поля.
 *
 * Студент создаёт интерфейс com.example.jpalab.dto.CategoryProductPrice
 * (getCategoryName, getProductName, getPrice, getStockQuantity) и метод в
 * ProductRepository: JOIN p.category c, WHERE по имени категории и
 * p.price BETWEEN :from AND :to (границы включены), сортировка
 * c.name, p.price, p.name.
 */
@DataJpaTest
class TASK09Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void joinProjectionWithCategoryAndPriceRange() {
        Category a = categoryRepository.saveAndFlush(Category.builder().name("A").build());
        Category b = categoryRepository.saveAndFlush(Category.builder().name("B").build());
        productRepository.save(Product.builder()
                .name("A1").price(new BigDecimal("100.00")).stockQuantity(1).category(a).build());
        productRepository.save(Product.builder()
                .name("A2").price(new BigDecimal("200.00")).stockQuantity(2).category(a).build());
        productRepository.save(Product.builder()
                .name("A3").price(new BigDecimal("300.00")).stockQuantity(3).category(a).build());
        productRepository.save(Product.builder()
                .name("A4").price(new BigDecimal("400.00")).stockQuantity(4).category(a).build());
        productRepository.save(Product.builder()
                .name("B1").price(new BigDecimal("150.00")).stockQuantity(1).category(b).build());

        List<?> rows = TaskTestSupport.invoke(productRepository, "categoryProductPrices",
                new Class<?>[]{String.class, BigDecimal.class, BigDecimal.class},
                "A", new BigDecimal("100.00"), new BigDecimal("300.00"));

        // категория B отсечена фильтром; A4 (400) — за диапазоном;
        // границы 100 и 300 включены (BETWEEN)
        assertThat(rows).hasSize(3);
        for (Object row : rows) {
            assertProjectedType(row, "com.example.jpalab.dto.CategoryProductPrice");
        }

        assertThat(call(rows.get(0), "getCategoryName")).isEqualTo("A");
        assertThat(call(rows.get(0), "getProductName")).isEqualTo("A1");
        assertThat((BigDecimal) call(rows.get(0), "getPrice")).isEqualByComparingTo("100.00");
        assertThat((Integer) call(rows.get(0), "getStockQuantity")).isEqualTo(1);
        assertThat(call(rows.get(1), "getProductName")).isEqualTo("A2");
        assertThat((BigDecimal) call(rows.get(1), "getPrice")).isEqualByComparingTo("200.00");
        assertThat((Integer) call(rows.get(1), "getStockQuantity")).isEqualTo(2);
        assertThat(call(rows.get(2), "getProductName")).isEqualTo("A3");
        assertThat((BigDecimal) call(rows.get(2), "getPrice")).isEqualByComparingTo("300.00");
        assertThat((Integer) call(rows.get(2), "getStockQuantity")).isEqualTo(3);
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
