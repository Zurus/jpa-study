# Задание 03. Конвейер: bulk UPDATE → bulk DELETE
- Тема: @Modifying
- Difficulty: 3
- Что нужно сделать: Новые методы не нужны. Тест уже создан (`TASK03Test`) и использует существующие методы `ProductRepository`: `updateStockForExpensive(stock, minPrice)` — bulk UPDATE «stock = :stock у товаров с ценой >= :minPrice» и `deleteOutOfStock()` — bulk DELETE «всё с stock = 0». Сценарий: три товара с ценами 10/30/40 и stock 1/2/3; сначала «обнулить» stock у дорогих (цена >= 25), затем удалить все товары с нулевым stock. Наблюдайте возвращаемые значения и итоговое состояние БД.
- Стартовый код:
```java
// Новые методы не нужны: тест использует уже существующие методы
// ProductRepository (updateStockForExpensive и deleteOutOfStock).
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t09.TASK03Test#updateThenDeletePipeline
```
- Критерии приёмки: Тест `updateThenDeletePipeline` зелёный: первый вызов возвращает 2 (P2 и P3 получили stock 0), второй — 2; в БД остался только P1 со stock 1; в SQL-логе bulk UPDATE, за которым bulk DELETE (два bulk-вызова подряд).
- Подсказка (не решение): Bulk-операции комбинируются как обычный SQL-скрипт: первый запрос готовит данные, второй их убирает; каждый возвращает число затронутых строк.
