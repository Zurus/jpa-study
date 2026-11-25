# Задание 08. Ловушка: порядок аргументов конструкторного выражения

Тема: 10 — Проекции
Сложность: 8/10

## Цель
Record `PriceStockAndName` объявлен в НЕ «естественном» порядке компонентов:
`(BigDecimal price, int stockQuantity, String name)`. Аргументы в
`SELECT new ...` обязаны совпадать с порядком компонентов, иначе запрос не
соберётся или поля перепутаются. Тест проверяет каждое поле номинально
(по имени селектора), поэтому перепутанный порядок сразу виден.

## Входные данные (тест самодостаточен)
4 товара: `A` 30.00 stock 5, `B` 70.00 stock 3, `C` 90.00 stock 1,
`D` 100.00 stock 9. Метод вызывается с `maxPrice = 100.00`.

## Что нужно сделать
1. Создайте record в пакете `com.example.jpalab.dto` (именно в этом порядке!):
   ```java
   public record PriceStockAndName(BigDecimal price, int stockQuantity, String name) {
   }
   ```
2. В `ProductRepository` добавьте метод:
   - сигнатура: `List<PriceStockAndName> priceStockNameForPricedBelow(@Param("maxPrice") BigDecimal maxPrice)`
   - `@Query`:
     `SELECT new com.example.jpalab.dto.PriceStockAndName(p.price, p.stockQuantity, p.name) FROM Product p WHERE p.price < :maxPrice ORDER BY p.name ASC`
   - строго меньше: `D` (ровно 100.00) не попадает;
   - порядок аргументов: price, stockQuantity, name — как в record.

## Стартовый код
```java
// src/main/java/com/example/jpalab/dto/PriceStockAndName.java (новый файл)

public record PriceStockAndName(BigDecimal price, int stockQuantity, String name) {
}
```
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: реализовать — @Query, SELECT new
//       com.example.jpalab.dto.PriceStockAndName(p.price, p.stockQuantity, p.name),
//       price < :maxPrice, ORDER BY p.name ASC
List<PriceStockAndName> priceStockNameForPricedBelow(@Param("maxPrice") BigDecimal maxPrice);
```

## Проверка
Тест: `src/test/java/com/example/jpalab/tasks/10-projections/TASK08Test.java`
Команда: `mvn -Dtest=com.example.jpalab.tasks.t10.TASK08Test#constructorArgumentOrderMatters`

Тест красный до реализации, зелёный после правильной.
