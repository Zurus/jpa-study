# Задание 03. Record DTO через конструкторное выражение в JPQL

Тема: 10 — Проекции
Сложность: 3/10

## Цель
Использовать конструкторное выражение (`SELECT new ...`) в существующий
record `com.example.jpalab.dto.ProductNamePrice(String name, BigDecimal price)`.
Record даёт value-семантику: сравнение по содержимому, доступ к полям через
селекторы name()/price().

## Входные данные (тест самодостаточен)
4 товара без категорий: `P300` 300.00 stock 2, `P100` 100.00 stock 7,
`P200` 200.00 stock 0, `P50` 50.00 stock 9. Метод вызывается с
`minStock = 2`.

## Что нужно сделать
1. В `ProductRepository` добавьте метод:
   - сигнатура: `List<ProductNamePrice> cheapestInStock(@Param("minStock") int minStock)`
   - `@Query`:
     `SELECT new com.example.jpalab.dto.ProductNamePrice(p.name, p.price) FROM Product p WHERE p.stockQuantity >= :minStock ORDER BY p.price ASC, p.name ASC`
   - граница включена: `>=`; сортировка — самые дешёвые первыми.

## Стартовый код
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: реализовать — @Query, SELECT new
//       com.example.jpalab.dto.ProductNamePrice(p.name, p.price),
//       stockQuantity >= :minStock, ORDER BY p.price ASC, p.name ASC
List<ProductNamePrice> cheapestInStock(@Param("minStock") int minStock);
```

## Проверка
Тест: `src/test/java/com/example/jpalab/tasks/10-projections/TASK03Test.java`
Команда: `mvn -Dtest=com.example.jpalab.tasks.t10.TASK03Test#recordProjectionViaConstructorExpression`

Тест красный до реализации, зелёный после правильной.
