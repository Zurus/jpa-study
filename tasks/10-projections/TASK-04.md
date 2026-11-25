# Задание 04. Конструкторное выражение в класс с полем из JOIN

Тема: 10 — Проекции
Сложность: 4/10

## Цель
Собрать DTO-класс (не record) с конструктором из 5 аргументов, где последний
аргумент — поле из связанной сущности (`p.category.id`). Товар без категории
останется в выборке, а его `categoryId` будет null. Используется существующий
`com.example.jpalab.dto.ProductDto` (порядок полей: id, name, price,
stockQuantity, categoryId).

## Входные данные (тест самодостаточен)
Категории `CA`, `CB`; товары: `Alpha` 100.00 stock 1 (CA), `Beta` 200.00
stock 2 (CB), `Gamma` 300.00 stock 3 (без категории). Метод вызывается с
`minPrice = 100.00`.

## Что нужно сделать
1. В `ProductRepository` добавьте метод:
   - сигнатура: `List<ProductDto> fullDtosForPricedAtLeast(@Param("minPrice") BigDecimal minPrice)`
   - `@Query`:
     `SELECT new com.example.jpalab.dto.ProductDto(p.id, p.name, p.price, p.stockQuantity, p.category.id) FROM Product p WHERE p.price >= :minPrice ORDER BY p.price DESC, p.id ASC`
   - порядок аргументов конструктора: id, name, price, stockQuantity, categoryId;
   - `Gamma` (без категории) должен попасть в выборку с `categoryId = null`.

## Стартовый код
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: реализовать — @Query, SELECT new com.example.jpalab.dto.ProductDto(
//       p.id, p.name, p.price, p.stockQuantity, p.category.id),
//       price >= :minPrice, ORDER BY p.price DESC, p.id ASC
List<ProductDto> fullDtosForPricedAtLeast(@Param("minPrice") BigDecimal minPrice);
```

## Проверка
Тест: `src/test/java/com/example/jpalab/tasks/10-projections/TASK04Test.java`
Команда: `mvn -Dtest=com.example.jpalab.tasks.t10.TASK04Test#classDtoProjectionWithJoinField`

Тест красный до реализации, зелёный после правильной.
