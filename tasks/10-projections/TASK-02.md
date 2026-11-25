# Задание 02. Open interface projection через @Query (существующий интерфейс)

Тема: 10 — Проекции
Сложность: 2/10

## Цель
Использовать open interface projection: JPQL выбирает скалярные поля, а
Spring Data сам собирает объекты-прокси интерфейса по getter'ам — без
конструктора в JPQL и без полных сущностей в памяти. Используется
существующий интерфейс `ProductRepository.ProductSummary`
(getter'ы getName, getPrice, getStockQuantity).

## Входные данные (тест самодостаточен)
Категория `SummaryCat` (создаётся в тесте) и 3 товара: `A` 10.00 stock 0,
`B` 20.00 stock 5, `C` 30.00 stock 5. Метод вызывается с `minStock = 5`.

## Что нужно сделать
1. В `ProductRepository` добавьте метод:
   - сигнатура: `List<ProductSummary> summariesInStockAtLeast(@Param("minStock") int minStock)`
   - `@Query`: `SELECT p.name AS name, p.price AS price, p.stockQuantity AS stockQuantity FROM Product p WHERE p.stockQuantity >= :minStock ORDER BY p.name ASC`
   - алиасы в SELECT обязательны: Spring Data сопоставляет имена столбцов
     со свойствами интерфейса (name, price, stockQuantity) — без алиасов
     прокси получит null; граница включена: `>=`.

## Стартовый код
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: реализовать — @Query, SELECT p.name AS name, p.price AS price,
//       p.stockQuantity AS stockQuantity, stockQuantity >= :minStock,
//       ORDER BY p.name ASC
List<ProductSummary> summariesInStockAtLeast(@Param("minStock") int minStock);
```

## Проверка
Тест: `src/test/java/com/example/jpalab/tasks/10-projections/TASK02Test.java`
Команда: `mvn -Dtest=com.example.jpalab.tasks.t10.TASK02Test#openInterfaceProjectionViaQuery`

Тест красный до реализации, зелёный после правильной.
