# Задание 07. flushAutomatically: запрос видит незаflushенные изменения
- Тема: @Modifying
- Difficulty: 7
- Что нужно сделать: Новые методы не нужны. Тест уже создан (`TASK07Test`) и демонстрирует флаг `flushAutomatically = true` на существующем методе `updateStockForExpensive(stock, minPrice)` из `ProductRepository` (bulk UPDATE «stock = :stock WHERE price >= :minPrice»). Сценарий: цена товара A изменена в PC (100 → 300), но изменение НЕ flush-нуто; затем выполняется bulk UPDATE «stock = 0, где цена >= 250».
- Стартовый код:
```java
// Новые методы не нужны: тест использует уже существующий метод
// ProductRepository.updateStockForExpensive(stock, minPrice).
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t09.TASK07Test#flushAutomaticallySeesUnflushedChanges
```
- Критерии приёмки: Тест `flushAutomaticallySeesUnflushedChanges` зелёный: метод возвращает 1 (а не 0!) — bulk-запрос увидел незаflushенную цену 300; в БД (native SELECT) у A stock 0, у B stock 6 (B не тронут, 200 < 250); в SQL-логе ПЕРЕД bulk UPDATE виден вызов `update products` для pending-изменения товара A (именно это делает flushAutomatically).
- Подсказка (не решение): Bulk-запрос, как и любой запрос, выполняется против БД: если изменения из PC не выгружены, WHERE-условие видит старые данные.
