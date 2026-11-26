# Тема 12. Нюансы: Bean Validation, Auditing, @Version, оптимистичная блокировка

Тема посвящена "мелким" механизмам JPA, которые ломают код на проде:
когда именно срабатывает Bean Validation, что заполняет auditing-поля,
как ведёт себя `@Version` (в том числе при bulk-обновлениях) и какое
исключение бросает Spring при конфликте конкурентных обновлений.

## Что уже есть в проекте (добавлять не нужно)

- `spring-boot-starter-validation` в `pom.xml` — Bean Validation активна.
- `Product`: `@NotBlank name`, `@PositiveOrZero price/stockQuantity`,
  `@CreatedDate createdAt`, `@LastModifiedDate updatedAt`,
  `@EntityListeners(AuditingEntityListener.class)`, `@Version version`.
- `Category`: `@NotBlank name`.
- `@EnableJpaAuditing` — уже на `JpaLabApplication`.

## Задания

| № | Difficulty | Что проверяем | Тип |
|---|-----------|----------------|------|
| 01 | 1 | `@Size(max = 100)` на `Product.name` (добавить студенту) | реализация |
| 02 | 2 | `@Size(max = 50)` на `Category.name` (добавить студенту) | реализация |
| 03 | 3 | Валидация — на `flush`, а не на `setter`; UPDATE не выполнен | наблюдение |
| 04 | 4 | "Пустой" flush не двигает `updated_at` и не поднимает `version` | наблюдение |
| 05 | 5 | Аудит в `Order`: `@CreatedDate createdAt` (добавить студенту) | реализация |
| 06 | 6 | Устаревший `save` через репозиторий → `OptimisticLockingFailureException` | наблюдение |
| 07 | 7 | Сервис `loseTheRace` — "гонка" двух обновлений | реализация |
| 08 | 8 | Сервис `saveWithRetry` — retry-паттерн после конфликта | реализация |
| 09 | 9 | Bulk `@Modifying` не трогает `version` — "старое" JPA-обновление проходит | наблюдение |
| 10 | 10 | Сервис `fullVersioningWalk` — validation + auditing + @Version в одном прогоне | реализация |

Все 10 заданий — красные до реализации (нужен код в `src/main/java`):
задания 01, 02, 05 — добавить аннотации в сущности; задания 03, 04, 06, 07, 08, 10 —
методы в `ProductVersioningService`; задание 09 — `@Modifying`-метод в
`ProductRepository`. Тесты созданы заранее и проверяют поведение после flush/commit.

## Контракт сервиса для заданий 03, 04, 06, 07, 08, 10

Класс `com.example.jpalab.service.ProductVersioningService`:

- аннотация `@Service` (имя бина по умолчанию — `productVersioningService`);
- зависимости: `EntityManager`, `ProductRepository`, `PlatformTransactionManager`;
- КАЖДЫЙ шаг метода — своя транзакция с `PROPAGATION_REQUIRES_NEW`
  (например, `TransactionTemplate` с `setPropagationBehavior(
  TransactionDefinition.PROPAGATION_REQUIRES_NEW)`);
- исключения ловятся ВНУТРИ метода, на результат — возвращаемое значение;
- конкурентное обновление проигравшего должно идти через
  `productRepository.save(...)` — только на границе Spring Data
  Hibernate'овский `StaleObjectStateException` транслируется в
  `org.springframework.dao.OptimisticLockingFailureException`
  (через `em.merge` прилетит JPA-`OptimisticLockException`).

### Методы

```java
// Задание 03: в новой транзакции (REQUIRES_NEW) load(id),
// set newName, commit; если валидация отклоняет — ловить и возвращать
// имя класса пойманного исключения, иначе "none".
String updateProductName(Long id, String newName)

// Задание 04: в новой транзакции (REQUIRES_NEW) find(id) и em.flush()
// БЕЗ изменений сущности; return product.
Product loadAndFlushProduct(Long id)

// Задание 06: T1 REQUIRES_NEW: find(id), setPrice(basePrice), commit.
// T2 REQUIRES_NEW: load(id), setPrice(stalePrice), productRepository.save —
// при конфликте ловить и return имя класса пойманного исключения.
String staleSaveExceptionType(Long id, BigDecimal basePrice, BigDecimal stalePrice)

// Задание 07:
// T1: find(id) — "читатель" забирает копию (version = 0);
// T2: find(id), setPrice(winnerPrice), commit — "победитель" (version 0 -> 1);
// T3: stale.setPrice(loserPrice), productRepository.save(stale) —
//     проигравший ловит OptimisticLockingFailureException.
// Возврат: [0] Long — итоговый version, [1] BigDecimal — итоговая цена,
//          [2] Boolean — поймал ли проигравший исключение.
public Object[] loseTheRace(Long id, BigDecimal winnerPrice, BigDecimal loserPrice)

// Задание 08:
// ПЕРВАЯ попытка сохраняет переданный экземпляр БЕЗ перезагрузки;
// при OptimisticLockingFailureException — новый REQUIRES_NEW,
// em.find(id), setPrice(targetPrice), повтор.
// attempts — число реально выполненных productRepository.save.
// Возврат: [0] Integer — attempts, [1] Long — итоговый version,
//          [2] BigDecimal — итоговая цена.
public Object[] saveWithRetry(Product product, BigDecimal targetPrice, int maxAttempts)

// Задание 10:
// 1) persist(name, price) в своей REQUIRES_NEW;
// 2) читать created_at/updated_at — обе не null -> "auditingSet";
// 3) "читатель" забирает копию (version = 0);
// 4) "победитель": setPrice(price + 10.00), commit -> "versionAfterRace";
// 5) "проигравший" (копия из шага 3): setPrice(price + 20.00),
//    productRepository.save -> поймать -> "loserCaught";
// 6) retry: setPrice(price + 30.00), commit -> "finalVersion";
// 7) validation: managed-копии setPrice(-1.00), em.flush ->
//    поймать ConstraintViolationException -> "validationCaught";
// 8) native-чтение: "finalPrice", "updatedAtMoved"
//    (updated_at после шага 4 строго больше, чем до него).
// Возврат: Map<String, Object> с ключами: id, auditingSet, versionAfterRace,
//          loserCaught, finalPrice, finalVersion, validationCaught,
//          updatedAtMoved.
public Map<String, Object> fullVersioningWalk(String name, BigDecimal price)
```

## Ключевые факты темы (справка)

- **Bean Validation в JPA**: проверка выполняется на `flush` (до выполнения
  SQL), а не на `persist` и тем более не на `setter`. Нарушение бросает
  `jakarta.validation.ConstraintViolationException`, транзакция откатывается,
  строка/UPDATE в БД не попадает.
- **Auditing**: `@CreatedDate` заполняется только при INSERT
  (`updatable = false`), `@LastModifiedDate` — при INSERT и при каждом
  реальном UPDATE. Если UPDATE в БД не ушёл (сущность не dirty, или
  flush "пустой"), поля не меняются.
- **@Version**: каждый JPA-UPDATE (dirty flushing / merge) поднимает
  version на 1 и добавляет `WHERE version = ?` в SQL. Если строк
  обновилось 0 — конфликт: через `EntityManager` это JPA
  `OptimisticLockException`, через Spring Data репозиторий —
  `OptimisticLockingFailureException`.
- **Bulk `@Modifying`**: обычный SQL UPDATE мимо Hibernate — не знает о
  `@Version`: version не поднимает, конфликтов не создаёт. Но и защиты
  от "перетирания" не даёт.

## Команды проверки

```bash
mvn -Dtest=com.example.jpalab.tasks.t12.TASK01Test#tooLongNameRejectedByValidation
mvn -Dtest=com.example.jpalab.tasks.t12.TASK02Test#tooLongCategoryNameRejectedByValidation
mvn -Dtest=com.example.jpalab.tasks.t12.TASK03Test#validationFiresOnFlushNotOnSetter
mvn -Dtest=com.example.jpalab.tasks.t12.TASK04Test#noOpFlushDoesNotBumpUpdatedAt
mvn -Dtest=com.example.jpalab.tasks.t12.TASK05Test#orderCreatedAtFilledByAuditing
mvn -Dtest=com.example.jpalab.tasks.t12.TASK06Test#staleSaveViaRepositoryThrowsOptimisticLockingFailureException
mvn -Dtest=com.example.jpalab.tasks.t12.TASK07Test#loseTheRace
mvn -Dtest=com.example.jpalab.tasks.t12.TASK08Test#saveWithRetrySucceedsOnSecondAttempt
mvn -Dtest=com.example.jpalab.tasks.t12.TASK09Test#bulkUpdateBypassesVersion
mvn -Dtest=com.example.jpalab.tasks.t12.TASK10Test#fullVersioningWalk
```
