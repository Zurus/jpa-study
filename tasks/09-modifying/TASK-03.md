# Задание 03. Конвейер: bulk UPDATE → bulk DELETE
- Тема: @Modifying
- Difficulty: 3
- Что нужно сделать: Добавьте в `ProductRepository` два bulk-метода: (1) `int zeroStockForPricedAtLeast(@Param("minPrice") BigDecimal minPrice)` — `@Modifying` bulk UPDATE «stock = 0 у товаров с ценой >= :minPrice» (граница включена), возвращает число затронутых строк; (2) `int deleteZeroStockProducts()` — `@Modifying` bulk DELETE «все товары с stock = 0». Заранее созданный тест (TASK03Test) прогоняет конвейер: три товара с ценами 10/30/40 и stock 1/2/3; сначала «обнулить» stock у дорогих (цена >= 25), затем удалить все товары с нулевым stock.
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Modifying + @Query (JPQL) — UPDATE products SET stock = 0 WHERE price >= :minPrice
int zeroStockForPricedAtLeast(@Param("minPrice") BigDecimal minPrice);

// TODO: @Modifying + @Query (JPQL) — DELETE где stock = 0
int deleteZeroStockProducts();
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t09.TASK03Test#updateThenDeletePipeline
```
- Критерий приёмки: Тест: первый вызов (`zeroStockForPricedAtLeast(25.00)`) возвращает 2 (P2 и P3 получили stock 0), второй (`deleteZeroStockProducts()`) — 2; в БД остался только P1 со stock 1; в SQL-логе bulk UPDATE, за которым bulk DELETE (два bulk-вызова подряд).
- Подсказка (не решение): Bulk-операции комбинируются как обычный SQL-скрипт: первый запрос готовит данные, второй их убирает; каждый возвращает число затронутых строк.
