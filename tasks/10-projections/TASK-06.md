# Задание 06. Проекция с JOIN: сортировка по полю связанной сущности (новый record)

Тема: 10 — Проекции
Сложность: 6/10

## Цель
Собрать record-проекцию (name товара, ИМЯ категории, цена) и отсортировать
результат сначала по ИМЕНИ КАТЕГОРИИ (поле из JOIN), затем по цене desc.
Сортировка по полю связанной сущности — частый запрос, который нельзя сделать
деривированным методом.

## Входные данные (тест самодостаточен)
Категории `A`, `B`; товары: `A Low` 100.00 (A), `A High` 300.00 (A),
`B Mid` 200.00 (B). Метод вызывается с `minPrice = 100.00`.

## Что нужно сделать
1. Создайте record в пакете `com.example.jpalab.dto`:
   ```java
   public record PricedInCategory(String productName, String categoryName, BigDecimal price) {
   }
   ```
2. В `ProductRepository` добавьте метод:
   - сигнатура: `List<PricedInCategory> pricedInCategories(@Param("minPrice") BigDecimal minPrice)`
   - `@Query`:
     `SELECT new com.example.jpalab.dto.PricedInCategory(p.name, c.name, p.price) FROM Product p JOIN p.category c WHERE p.price >= :minPrice ORDER BY c.name ASC, p.price DESC, p.id ASC`
   - порядок аргументов конструктора: productName, categoryName, price;
   - граница включена: `>=`.

## Стартовый код
```java
// src/main/java/com/example/jpalab/dto/PricedInCategory.java (новый файл)

public record PricedInCategory(String productName, String categoryName, BigDecimal price) {
}
```
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: реализовать — @Query, SELECT new
//       com.example.jpalab.dto.PricedInCategory(p.name, c.name, p.price),
//       JOIN p.category c, price >= :minPrice,
//       ORDER BY c.name ASC, p.price DESC, p.id ASC
List<PricedInCategory> pricedInCategories(@Param("minPrice") BigDecimal minPrice);
```

## Проверка
Тест: `src/test/java/com/example/jpalab/tasks/10-projections/TASK06Test.java`
Команда: `mvn -Dtest=com.example.jpalab.tasks.t10.TASK06Test#joinProjectionSortedByJoinedField`

Тест красный до реализации, зелёный после правильной.
