# Задание 07. LEFT JOIN в проекции: товары без категории

Тема: 10 — Проекции
Сложность: 7/10

## Цель
Показать, что INNER JOIN p.category тихо исключает из выборки товары без
категории. LEFT JOIN сохраняет их, а имя категории в проекции равно null.
Метод без параметров: проекция «имя товара + имя категории (или null)».

## Входные данные (тест самодостаточен)
Категория `WithCat`; товары: `P1` 10.00 (WithCat), `P2` 20.00 (WithCat),
`P3` 30.00 (без категории). Метод вызывается без аргументов.

## Что нужно сделать
1. Создайте интерфейс в пакете `com.example.jpalab.dto`:
   ```java
   public interface ProductNameCategory {
       String getProductName();
       String getCategoryName();
   }
   ```
2. В `ProductRepository` добавьте метод:
   - сигнатура: `List<ProductNameCategory> namesWithOptionalCategory()`
   - `@Query`:
     `SELECT p.name AS productName, c.name AS categoryName FROM Product p LEFT JOIN p.category c ORDER BY p.name ASC`
   - именно LEFT JOIN: при INNER JOIN P3 не попадёт в выборку и тест не пройдёт;
   - у P3 `getCategoryName()` вернёт null.

## Стартовый код
```java
// src/main/java/com/example/jpalab/dto/ProductNameCategory.java (новый файл)

public interface ProductNameCategory {
    String getProductName();
    String getCategoryName();
}
```
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: реализовать — @Query, SELECT p.name AS productName, c.name AS
//       categoryName, LEFT JOIN p.category c, ORDER BY p.name ASC
List<ProductNameCategory> namesWithOptionalCategory();
```

## Проверка
Тест: `src/test/java/com/example/jpalab/tasks/10-projections/TASK07Test.java`
Команда: `mvn -Dtest=com.example.jpalab.tasks.t10.TASK07Test#leftJoinProjectionKeepsNullCategory`

Тест красный до реализации, зелёный после правильной.
