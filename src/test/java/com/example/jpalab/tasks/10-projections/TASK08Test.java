package com.example.jpalab.tasks.t10;

import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 10, задание 08. Ловушка: порядок аргументов конструкторного выражения.
 *
 * Record com.example.jpalab.dto.PriceStockAndName(BigDecimal price, int
 * stockQuantity, String name) объявлен в НЕ «естественном» порядке
 * (price, stock, name). Аргументы в SELECT new ... обязаны совпадать с
 * порядком компонентов record, иначе Hibernate не соберёт запрос. Тест
 * проверяет значения каждого поля НОМИНАЛЬНО (price(), stockQuantity(),
 * name()), поэтому перепутанный порядок поймает именно он.
 */
@DataJpaTest
class TASK08Test {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void constructorArgumentOrderMatters() {
        productRepository.save(Product.builder()
                .name("A").price(new BigDecimal("30.00")).stockQuantity(5).build());
        productRepository.save(Product.builder()
                .name("B").price(new BigDecimal("70.00")).stockQuantity(3).build());
        productRepository.save(Product.builder()
                .name("C").price(new BigDecimal("90.00")).stockQuantity(1).build());
        productRepository.save(Product.builder()
                .name("D").price(new BigDecimal("100.00")).stockQuantity(9).build());

        // строго меньше 100: D (100.00) не попадает; сортировка по имени asc
        List<?> rows = TaskTestSupport.invoke(productRepository, "priceStockNameForPricedBelow",
                new Class<?>[]{BigDecimal.class}, new BigDecimal("100.00"));

        assertThat(rows).hasSize(3);
        for (Object row : rows) {
            assertProjectedType(row, "com.example.jpalab.dto.PriceStockAndName");
            // типы полей: price — BigDecimal, stockQuantity — int, name — String
            assertThat(call(row, "price")).isInstanceOf(BigDecimal.class);
            assertThat(call(row, "stockQuantity")).isInstanceOf(Integer.class);
            assertThat(call(row, "name")).isInstanceOf(String.class);
        }

        // значения по именам селекторов (порядок компонентов: price, stock, name)
        assertThat(call(rows.get(0), "name")).isEqualTo("A");
        assertThat((BigDecimal) call(rows.get(0), "price")).isEqualByComparingTo("30.00");
        assertThat((Integer) call(rows.get(0), "stockQuantity")).isEqualTo(5);
        assertThat(call(rows.get(1), "name")).isEqualTo("B");
        assertThat((BigDecimal) call(rows.get(1), "price")).isEqualByComparingTo("70.00");
        assertThat((Integer) call(rows.get(1), "stockQuantity")).isEqualTo(3);
        assertThat(call(rows.get(2), "name")).isEqualTo("C");
        assertThat((BigDecimal) call(rows.get(2), "price")).isEqualByComparingTo("90.00");
        assertThat((Integer) call(rows.get(2), "stockQuantity")).isEqualTo(1);
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
