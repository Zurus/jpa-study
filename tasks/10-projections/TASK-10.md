# Задание 10. Один и тот же запрос в двух формах: record против интерфейса

Тема: 10 — Проекции
Сложность: 10/10

## Цель
Финальное сравнение форм проекций. Два метода с одинаковыми данными
(name, price; price >= :minPrice; ORDER BY name, id): один — конструкторное
выражение в существующий record `ProductNamePrice`, второй — скалярный SELECT
в новый open interface `NamePriceView`. Содержимое выборок совпадает; типы
разные: record — value-объект (equals по содержимому, `isRecord() == true`),
интерфейс — JDK-прокси (не record, без value-equality).

## Входные данные (тест самодостаточен)
4 товара: `A` 100.00 stock 3, `B` 200.00 stock 3, `C` 300.00 stock 0,
`D` 50.00 stock 1. Оба метода вызываются с `minPrice = 100.00`.

## Что нужно сделать
1. Создайте интерфейс в пакете `com.example.jpalab.dto`:
   ```java
   public interface NamePriceView {
       String getName();
       BigDecimal getPrice();
   }
   ```
2. В `ProductRepository` добавьте ДВА метода с одинаковой логикой:
   - `List<ProductNamePrice> namePricePairsForPricedAtLeast(@Param("minPrice") BigDecimal minPrice)`
     `@Query`:
     `SELECT new com.example.jpalab.dto.ProductNamePrice(p.name, p.price) FROM Product p WHERE p.price >= :minPrice ORDER BY p.name ASC, p.id ASC`
   - `List<NamePriceView> namePricePairsAsInterface(@Param("minPrice") BigDecimal minPrice)`
     `@Query`:
     `SELECT p.name AS name, p.price AS price FROM Product p WHERE p.price >= :minPrice ORDER BY p.name ASC, p.id ASC`
   - `D` (50.00) не попадает ни в одну выборку; граница 100 включена;
   - алиасы в SELECT обязательны: имена столбцов (name, price) должны
     совпадать со свойствами интерфейса (getName, getPrice).

## Стартовый код
```java
// src/main/java/com/example/jpalab/dto/NamePriceView.java (новый файл)

public interface NamePriceView {
    String getName();
    BigDecimal getPrice();
}
```
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: реализовать оба метода — record и интерфейс, одна и та же логика:
//       price >= :minPrice, ORDER BY p.name ASC, p.id ASC
List<ProductNamePrice> namePricePairsForPricedAtLeast(@Param("minPrice") BigDecimal minPrice);
List<NamePriceView> namePricePairsAsInterface(@Param("minPrice") BigDecimal minPrice);
```

## Проверка
Тест: `src/test/java/com/example/jpalab/tasks/10-projections/TASK10Test.java`
Команда: `mvn -Dtest=com.example.jpalab.tasks.t10.TASK10Test#sameQueryRecordVsInterface`

Тест красный до реализации, зелёный после правильной.
