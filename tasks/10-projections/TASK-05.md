# Задание 05. Проекция с JOIN: товар + имя категории (новый интерфейс)

Тема: 10 — Проекции
Сложность: 5/10

## Цель
Собрать проекцию с JOIN по связанной сущности: имя товара, ИМЯ КАТЕГОРИИ
(поле из JOIN-поля) и цена. Открыть новый интерфейс-проекцию и метод
репозитория, фильтрующий по имени категории.

## Входные данные (тест самодостаточен)
Категории `Audio`, `Home`; товары: `Headphones` 300.00 stock 1 (Audio),
`Mic` 100.00 stock 2 (Audio), `Speaker` 200.00 stock 1 (Home). Метод
вызывается с `categoryName = "Audio"`.

## Что нужно сделать
1. Создайте интерфейс в пакете `com.example.jpalab.dto`:
   ```java
   public interface ProductCategoryView {
       String getProductName();
       String getCategoryName();
       BigDecimal getPrice();
   }
   ```
2. В `ProductRepository` добавьте метод:
   - сигнатура: `List<ProductCategoryView> withCategoryName(@Param("categoryName") String categoryName)`
   - `@Query`:
     `SELECT p.name AS productName, c.name AS categoryName, p.price AS price FROM Product p JOIN p.category c WHERE c.name = :categoryName ORDER BY p.price DESC, p.name ASC`
   - алиасы в SELECT обязательны для каждого столбца: Spring Data сопоставляет
     имена столбцов со свойствами интерфейса (getProductName/getCategoryName/getPrice);
     без алиаса столбец получит имя null и getter вернёт null;
   - INNER JOIN: товары без категории не попадут (здесь все есть).

## Стартовый код
```java
// src/main/java/com/example/jpalab/dto/ProductCategoryView.java (новый файл)

public interface ProductCategoryView {
    String getProductName();
    String getCategoryName();
    BigDecimal getPrice();
}
```
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: реализовать — @Query, SELECT p.name AS productName, c.name AS
//       categoryName, p.price, JOIN p.category c, c.name = :categoryName,
//       ORDER BY p.price DESC, p.name ASC
List<ProductCategoryView> withCategoryName(@Param("categoryName") String categoryName);
```

## Проверка
Тест: `src/test/java/com/example/jpalab/tasks/10-projections/TASK05Test.java`
Команда: `mvn -Dtest=com.example.jpalab.tasks.t10.TASK05Test#projectionWithJoinToCategory`

Тест красный до реализации, зелёный после правильной.
