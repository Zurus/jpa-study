# Задание 03. Record DTO через конструкторное выражение в JPQL
- Тема: Проекции
- Difficulty: 3
- Что нужно сделать: В `ProductRepository` добавьте JPQL-метод `List<ProductNamePrice> cheapestInStock(@Param("minStock") int minStock)` — конструкторное выражение в СУЩЕСТВУЮЩИЙ record `com.example.jpalab.dto.ProductNamePrice(String name, BigDecimal price)`. В выборку попадают товары с остатком НЕ МЕНЬШЕ `:minStock` (граница включена), сортировка: цена по возрастанию (самые дешёвые первыми), тай-брейк — имя по возрастанию.
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Query — конструкторное выражение в ProductNamePrice,
//       stockQuantity >= :minStock, ORDER BY price ASC, name ASC
List<ProductNamePrice> cheapestInStock(@Param("minStock") int minStock);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t10.TASK03Test#recordProjectionViaConstructorExpression
```
- Критерий приёмки: Тест: 4 товара (стоки 2/7/0/9), вызов с minStock = 2 — 3 record-объекта в порядке возрастания цены (100.00, 200.00, 300.00), объект P200 (stock 0) отсутствует; поля доступны селекторами name()/price().
- Подсказка (не решение): в `SELECT new ...` указывается ПОЛНЫЙ класс record, а аргументы конструктора перечисляются в том же порядке, что компоненты record.
