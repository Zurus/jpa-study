# Задание 08. Ловушка: «устаревшая» сущность переписывает bulk UPDATE
- Тема: @Modifying
- Difficulty: 8
- Что нужно сделать: Новые методы не нужны. Тест уже создан (`TASK08Test`) и демонстрирует ловушку bulk UPDATE БЕЗ `clearAutomatically` (существующий метод `bumpAllPricesBy(delta)` из `ProductRepository` — «price += :delta у всех товаров»). Сценарий: товар A загружен в PC (цена 100), выполнен bulk +100 (в БД A = 200), затем у «устаревшего» managed-объекта A «невинно» меняется поле `stockQuantity = 5` и вызывается `em.flush()`.
- Стартовый код:
```java
// Новые методы не нужны: тест использует уже существующий метод
// ProductRepository.bumpAllPricesBy(delta).
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t09.TASK08Test#staleEntityOverwritesBulkUpdate
```
- Критерии приёмки: Тест `staleEntityOverwritesBulkUpdate` зелёный: после bulk UPDATE в БД A = 200.00; после изменения устаревшей сущности и `em.flush()` в БД у A price = 100.00 (старое значение «перетёрло» bulk-обновление) и stock = 5; у B price = 300.00 (не пострадал); в комментарии записано, почему так происходит (@DynamicUpdate не стоит → UPDATE содержит ВСЕ столбцы сущности, а PC ничего не знает о bulk UPDATE) и как это предотвратить (`clearAutomatically` или `em.clear()` после bulk-запроса).
- Подсказка (не решение): Dirty-checking при flush пишет в БД состояние сущности из PC целиком — если PC «не в курсе» bulk-изменений, он честно вернёт старые значения.
