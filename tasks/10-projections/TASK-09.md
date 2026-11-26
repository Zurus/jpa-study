# Задание 09. Проекция с JOIN: фильтр по категории + диапазон цен, 4 поля
- Тема: Проекции
- Difficulty: 9
- Что нужно сделать: (1) Создайте интерфейс-проекцию `CategoryProductPrice` в пакете `com.example.jpalab.dto` с getter'ами `String getCategoryName()`, `String getProductName()`, `BigDecimal getPrice()`, `int getStockQuantity()`. (2) В `ProductRepository` добавьте JPQL-метод `List<CategoryProductPrice> categoryProductPrices(@Param("categoryName") String categoryName, @Param("from") BigDecimal from, @Param("to") BigDecimal to)` — 4 поля через JOIN по категории (имя категории, имя товара, цена, остаток) с фильтром ОДНОВРЕМЕННО по имени категории и диапазону цен (BETWEEN, обе границы включены). Сортировка: имя категории ASC, цена ASC, имя товара ASC.
- Стартовый код:
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

// TODO: @Query — JOIN p.category, c.name = :categoryName,
//       p.price BETWEEN :from AND :to (включительно), 4 алиаса в SELECT,
//       ORDER BY c.name ASC, p.price ASC, p.name ASC
List<CategoryProductPrice> categoryProductPrices(
        @Param("categoryName") String categoryName,
        @Param("from") BigDecimal from,
        @Param("to") BigDecimal to);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t10.TASK09Test#joinProjectionWithCategoryAndPriceRange
```
- Критерий приёмки: Тест: категория A (100/200/300/400, сток 1..4) и B (150, сток 1); вызов с ("A", 100.00, 300.00) — ровно 3 строки A1, A2, A3 (B1 отсечён категорией, A4 — за диапазоном), границы 100.00 и 300.00 включены.
- Подсказка (не решение): диапазон «с обеими границами» в JPQL — один оператор на два параметра; алиасы в SELECT обязательны для сопоставления со свойствами интерфейса.
