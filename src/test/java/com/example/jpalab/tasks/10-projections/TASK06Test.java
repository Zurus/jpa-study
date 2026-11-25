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
 * Тема 10, задание 06. Проекция с JOIN: сортировка по полю связанной сущности.
 *
 * Студент создаёт record com.example.jpalab.dto.PricedInCategory(String
 * productName, String categoryName, BigDecimal price) и метод в
 * ProductRepository: конструкторное выражение (p.name, c.name, p.price),
 * JOIN p.category c, сортировка сначала по ИМЕНИ КАТЕГОРИИ (поле из JOIN),
 * затем по цене desc. Тест проверяет элементы рефлексивно.
 */
@DataJpaTest
class TASK06Test {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void joinProjectionSortedByJoinedField() {
        Category a = categoryRepository.saveAndFlush(Category.builder().name("A").build());
        Category b = categoryRepository.saveAndFlush(Category.builder().name("B").build());
        productRepository.save(Product.builder()
                .name("A Low").price(new BigDecimal("100.00")).stockQuantity(1).category(a).build());
        productRepository.save(Product.builder()
                .name("A High").price(new BigDecimal("300.00")).stockQuantity(1).category(a).build());
        productRepository.save(Product.builder()
                .name("B Mid").price(new BigDecimal("200.00")).stockQuantity(1).category(b).build());

        // price >= 100: все три (граница включена)
        List<?> rows = TaskTestSupport.invoke(productRepository, "pricedInCategories",
                new Class<?>[]{BigDecimal.class}, new BigDecimal("100.00"));

        assertThat(rows).hasSize(3);
        for (Object row : rows) {
            assertProjectedType(row, "com.example.jpalab.dto.PricedInCategory");
        }

        // сортировка: категория asc, внутри категории цена desc:
        // A: 300, 100; B: 200
        assertThat(call(rows.get(0), "productName")).isEqualTo("A High");
        assertThat(call(rows.get(0), "categoryName")).isEqualTo("A");
        assertThat((BigDecimal) call(rows.get(0), "price")).isEqualByComparingTo("300.00");
        assertThat(call(rows.get(1), "productName")).isEqualTo("A Low");
        assertThat(call(rows.get(1), "categoryName")).isEqualTo("A");
        assertThat((BigDecimal) call(rows.get(1), "price")).isEqualByComparingTo("100.00");
        assertThat(call(rows.get(2), "productName")).isEqualTo("B Mid");
        assertThat(call(rows.get(2), "categoryName")).isEqualTo("B");
        assertThat((BigDecimal) call(rows.get(2), "price")).isEqualByComparingTo("200.00");
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
