# Задание 01. Проекция полей: скалярный кортеж (name, price)

Тема: 10 — Проекции
Сложность: 1/10

## Цель
Вернуть из запроса только два поля (name, price) в виде `List<Object[]>`:
порядок элементов в массиве = порядок в SELECT, сортировка по цене desc с
тай-брейком по id, строгое условие по цене.

## Входные данные (тест самодостаточен)
5 товаров без категорий: `Cheap` 50.00, `Boundary` 100.00, `Mid` 150.00,
`High B` 250.00, `High A` 250.00 (последние два — с равной ценой, у `High B`
меньший id). Метод вызывается с `minPrice = 100.00`.

## Что нужно сделать
1. В `ProductRepository` добавьте метод:
   - сигнатура: `List<Object[]> namesAndPricesAbove(@Param("minPrice") BigDecimal minPrice)`
   - `@Query`: `SELECT p.name, p.price FROM Product p WHERE p.price > :minPrice ORDER BY p.price DESC, p.id ASC`
   - строго больше: товар с ценой ровно 100.00 не попадает;
   - порядок столбцов в кортеже: [0] name, [1] price.

## Стартовый код
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: реализовать — @Query, SELECT p.name, p.price, строго больше :minPrice,
//       ORDER BY p.price DESC, p.id ASC
List<Object[]> namesAndPricesAbove(@Param("minPrice") BigDecimal minPrice);
```

## Проверка
Тест: `src/test/java/com/example/jpalab/tasks/10-projections/TASK01Test.java`
Команда: `mvn -Dtest=com.example.jpalab.tasks.t10.TASK01Test#scalarTupleProjectionOrderByPriceDesc`

Тест красный до реализации, зелёный после правильной.
