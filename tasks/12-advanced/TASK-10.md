# Задание 10. Финал: полный прогон validation + auditing + @Version
- Тема: Нюансы (Bean Validation, Auditing, @Version)
- Difficulty: 10
- Что нужно сделать: В `ProductVersioningService` реализуйте метод `Map<String, Object> fullVersioningWalk(String name, BigDecimal price)` — "битва цен" одним вызовом, каждый шаг — своя `REQUIRES_NEW`-транзакция: (1) `persist` продукта с ценой `price`; (2) проверка аудита — `created_at` и `updated_at` не null → ключ `auditingSet`; (3) "читатель" забирает копию (`version = 0`); (4) "победитель": `price + 10.00`, commit → ключ `versionAfterRace`; (5) "проигравший" (копия из шага 3): `price + 20.00` через `productRepository.save`, поймать `OptimisticLockingFailureException` → ключ `loserCaught`; (6) retry: `price + 30.00`, commit → ключ `finalVersion`; (7) validation: managed-копии поставить `price = -1.00` и `em.flush`, поймать `ConstraintViolationException` → ключ `validationCaught` (цена в БД от этого не меняется); (8) native-чтение финала → ключи `finalPrice`, `updatedAtMoved` (`updated_at` после шага 4 строго больше, чем до него). Возврат — `Map<String, Object>` с ключами: `id`, `auditingSet`, `versionAfterRace`, `loserCaught`, `finalPrice`, `finalVersion`, `validationCaught`, `updatedAtMoved` (типы — в README).
- Стартовый код:
```java
// TODO: добавьте в ProductVersioningService:
public Map<String, Object> fullVersioningWalk(String name, BigDecimal price) {
    // TODO: шаги 1..8 — каждый в своей REQUIRES_NEW-транзакции,
    //       исключения (OptimisticLockingFailureException /
    //       ConstraintViolationException) ловятся внутри метода
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t12.TASK10Test#fullVersioningWalk
```
- Критерий приёмки: Тест: `id` > 0, `auditingSet = true`, `versionAfterRace = 1`, `loserCaught = true`, `finalPrice = base + 30.00`, `finalVersion = 2`, `validationCaught = true`, `updatedAtMoved = true`; независимая native-проверка: одна строка, цена base + 30.00, version = 2, `created_at` не null.
- Подсказка (не решение): шаги 5 и 7 "ломаются" НАМЕРЕННО — ловля их и есть суть задания; для `updatedAtMoved` сравнивайте `LocalDateTime` (H2 хранит TIMESTAMP(6)), а не String.
