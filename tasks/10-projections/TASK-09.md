# Задание 09. Проекция с JOIN: фильтр по категории + диапазон цен, 4 поля

Тема: 10 — Проекции
Сложность: 9/10

## Цель
Комбинированная проекция с JOIN: 4 поля (имя категории, имя товара, цена,
остаток), фильтр одновременно по имени категории и диапазону цен
(BETWEEN, границы включены), детерминированная трёхуровневая сортировка.

## Входные данные (тест самодостаточен)
Категории `A`, `B`; товары: `A1` 100.00 stock 1 (A), `A2` 200.00 stock 2 (A),
`A3` 300.00 stock 3 (A), `A4` 400.00 stock 4 (A), `B1` 150.00 stock 1 (B).
Метод вызывается с `("A", 100.00, 300.00)`.

## Что нужно сделать
1. Создайте интерфейс в пакете `com.example.jpalab.dto`:
   ```java
   public interface CategoryProductPrice {
       String getCategoryName();
       String getProductName();
       BigDecimal getPrice();
       int getStockQuantity();
   }
   ```
2. В `ProductRepository` добавьте метод:
   - сигнатура: `List<CategoryProductPrice> categoryProductPrices(@Param("categoryName") String categoryName, @Param("from") BigDecimal from, @Param("to") BigDecimal to)`
   - `@Query`:
     `SELECT c.name AS categoryName, p.name AS productName, p.price AS price, p.stockQuantity AS stockQuantity FROM Product p JOIN p.category c WHERE c.name = :categoryName AND p.price BETWEEN :from AND :to ORDER BY c.name ASC, p.price ASC, p.name ASC`
   - `B1` отсекается фильтром по категории; `A4` (400) — за диапазоном;
   - границы 100 и 300 включены (BETWEEN);
   - алиасы в SELECT обязательны для сопоставления со свойствами интерфейса.

## Стартовый код
```java
// src/main/java/com/example/jpalab/dto/CategoryProductPrice.java (новый файл)

public interface CategoryProductPrice {
    String getCategoryName();
    String getProductName();
    BigDecimal getPrice();
    int getStockQuantity();
}
```
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: реализовать — @Query, SELECT c.name AS categoryName, p.name AS
//       productName, p.price, p.stockQuantity, JOIN p.category c,
//       c.name = :categoryName AND p.price BETWEEN :from AND :to,
//       ORDER BY c.name ASC, p.price ASC, p.name ASC
List<CategoryProductPrice> categoryProductPrices(
        @Param("categoryName") String categoryName,
        @Param("from") BigDecimal from,
        @Param("to") BigDecimal to);
```

## Проверка
Тест: `src/test/java/com/example/jpalab/tasks/10-projections/TASK09Test.java`
Команда: `mvn -Dtest=com.example.jpalab.tasks.t10.TASK09Test#joinProjectionWithCategoryAndPriceRange`

Тест красный до реализации, зелёный после правильной.
