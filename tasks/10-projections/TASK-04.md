# Задание 04. Конструкторное выражение в класс с полем из JOIN
- Тема: Проекции
- Difficulty: 4
- Что нужно сделать: В `ProductRepository` добавьте JPQL-метод `List<ProductDto> fullDtosForPricedAtLeast(@Param("minPrice") BigDecimal minPrice)` — конструкторное выражение в СУЩЕСТВУЮЩИЙ класс `com.example.jpalab.dto.ProductDto` (порядок полей: id, name, price, stockQuantity, categoryId), где последний аргумент — поле СВЯЗАННОЙ сущности (id категории). В выборку попадают товары с ценой НЕ МЕНЬШЕ `:minPrice` (граница включена); товар без категории остаётся в выборке с `categoryId = null`. Сортировка: цена по убыванию, тай-брейк — id по возрастанию.
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Query — конструкторное выражение в ProductDto (5 аргументов,
//       последний — id категории), price >= :minPrice,
//       ORDER BY price DESC, id ASC
List<ProductDto> fullDtosForPricedAtLeast(@Param("minPrice") BigDecimal minPrice);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t10.TASK04Test#classDtoProjectionWithJoinField
```
- Критерий приёмки: Тест: 3 товара (100.00/CA, 200.00/CB, 300.00/без категории), вызов с 100.00 — 3 DTO в порядке 300.00, 200.00, 100.00; у Gamma (без категории) categoryId = null, остальные поля заполнены.
- Подсказка (не решение): доступ к полю связанной сущности из JPQL — через точку (`p.category.id`); для null-категории конструктор получит null в соответствующий аргумент.
