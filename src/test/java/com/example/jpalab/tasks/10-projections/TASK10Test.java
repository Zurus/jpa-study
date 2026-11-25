package com.example.jpalab.tasks.t10;

import com.example.jpalab.dto.ProductNamePrice;
import com.example.jpalab.entity.Product;
import com.example.jpalab.repository.ProductRepository;
import com.example.jpalab.tasks.TaskTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тема 10, задание 10. Один и тот же запрос в двух формах: record против
 * open interface projection.
 *
 * Две методы с ОДИНАКОВЫМ JPQL по данным (name, price; price >= :minPrice;
 * ORDER BY name, id):
 *  1) namePricePairsForPricedAtLeast — конструкторное выражение в существующий
 *     record ProductNamePrice;
 *  2) namePricePairsAsInterface — скалярный SELECT в новый интерфейс
 *     com.example.jpalab.dto.NamePriceView (getName, getPrice), который
 *     создаёт студент.
 * Содержимое совпадает; типы разные: record — value-объект (equals по
 * содержимому), интерфейс — JDK-прокси (не record, без value-equality).
 */
@DataJpaTest
class TASK10Test {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void sameQueryRecordVsInterface() {
        productRepository.save(Product.builder()
                .name("A").price(new BigDecimal("100.00")).stockQuantity(3).build());
        productRepository.save(Product.builder()
                .name("B").price(new BigDecimal("200.00")).stockQuantity(3).build());
        productRepository.save(Product.builder()
                .name("C").price(new BigDecimal("300.00")).stockQuantity(0).build());
        productRepository.save(Product.builder()
                .name("D").price(new BigDecimal("50.00")).stockQuantity(1).build());

        List<?> recordRows = TaskTestSupport.invoke(productRepository,
                "namePricePairsForPricedAtLeast", new Class<?>[]{BigDecimal.class},
                new BigDecimal("100.00"));
        List<?> interfaceRows = TaskTestSupport.invoke(productRepository,
                "namePricePairsAsInterface", new Class<?>[]{BigDecimal.class},
                new BigDecimal("100.00"));

        // price >= 100: D (50) не попадает; граница 100 включена; имя asc: A, B, C
        assertThat(recordRows).hasSize(3);
        assertThat(interfaceRows).hasSize(3);

        // содержимое (name, price) в обоих списках одинаковое, в одном порядке
        assertThat(pairs(interfaceRows)).containsExactlyElementsOf(pairsOfRecords(recordRows));

        // record: точный тип, value-семантика
        assertThat(recordRows.get(0)).isInstanceOf(ProductNamePrice.class);
        assertThat(recordRows.get(0).getClass().isRecord()).isTrue();
        assertThat(recordRows.get(0))
                .isEqualTo(new ProductNamePrice("A", new BigDecimal("100.00")));
        assertThat(((ProductNamePrice) recordRows.get(1)).name()).isEqualTo("B");
        assertThat(((ProductNamePrice) recordRows.get(2)).name()).isEqualTo("C");

        // интерфейс: прокси, реализующий NamePriceView, НЕ record
        for (Object row : interfaceRows) {
            assertProjectedType(row, "com.example.jpalab.dto.NamePriceView");
            assertThat(row.getClass().isRecord()).isFalse();
        }
        assertThat(call(interfaceRows.get(0), "getName")).isEqualTo("A");
        assertThat((BigDecimal) call(interfaceRows.get(0), "getPrice")).isEqualByComparingTo("100.00");
        assertThat(call(interfaceRows.get(1), "getName")).isEqualTo("B");
        assertThat((BigDecimal) call(interfaceRows.get(1), "getPrice")).isEqualByComparingTo("200.00");
        assertThat(call(interfaceRows.get(2), "getName")).isEqualTo("C");
        assertThat((BigDecimal) call(interfaceRows.get(2), "getPrice")).isEqualByComparingTo("300.00");
    }

    private static List<List<Object>> pairs(List<?> rows) {
        List<List<Object>> out = new ArrayList<>();
        for (Object row : rows) {
            out.add(List.of(call(row, "getName"), call(row, "getPrice")));
        }
        return out;
    }

    private static List<List<Object>> pairsOfRecords(List<?> rows) {
        List<List<Object>> out = new ArrayList<>();
        for (Object row : rows) {
            ProductNamePrice p = (ProductNamePrice) row;
            out.add(List.of(p.name(), p.price()));
        }
        return out;
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
