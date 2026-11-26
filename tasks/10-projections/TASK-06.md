# Задание 06. Проекция с JOIN: сортировка по полю связанной сущности (новый record)
- Тема: Проекции
- Difficulty: 6
- Что нужно сделать: (1) Создайте record в пакете `com.example.jpalab.dto`: `public record PricedInCategory(String productName, String categoryName, BigDecimal price) {}`. (2) В `ProductRepository` добавьте JPQL-метод `List<PricedInCategory> pricedInCategories(@Param("minPrice") BigDecimal minPrice)` — конструкторное выражение с JOIN по категории: имя товара, ИМЯ категории, цена; товары с ценой НЕ МЕНЬШЕ `:minPrice` (граница включена). Сортировка: сначала по ИМЕНИ КАТЕГОРИИ по возрастанию, затем цена по убыванию, тай-брейк — id по возрастанию.
- Стартовый код:
```java
// src/main/java/com/example/jpalab/dto/PricedInCategory.java (новый файл)

public record PricedInCategory(String productName, String categoryName, BigDecimal price) {
}
```
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Query — конструкторное выражение в PricedInCategory с JOIN p.category,
//       price >= :minPrice, ORDER BY имя категории ASC, price DESC, id ASC
List<PricedInCategory> pricedInCategories(@Param("minPrice") BigDecimal minPrice);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t10.TASK06Test#joinProjectionSortedByJoinedField
```
- Критерий приёмки: Тест: категории A (A Low 100.00, A High 300.00) и B (B Mid 200.00); вызов с 100.00 — 3 строки в порядке: A High (300.00), A Low (100.00), B Mid (200.00) — сначала все товары A (цена desc), потом B.
- Подсказка (не решение): сортировка по полю связанной сущности пишется по пути через JOIN-алиас (имя категории), а не по полю самого товара.
