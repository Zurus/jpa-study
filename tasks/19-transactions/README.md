# Тема 19. `@Transactional`: propagation и rollback

Границы транзакций в Spring Data JPA: аннотация `@Transactional`,
уровни propagation (REQUIRED / REQUIRES_NEW / MANDATORY), правила
rollback по типу исключения (`rollbackFor` / `noRollbackFor`), флаг
`readOnly`, программные границы `TransactionTemplate`, поведение метода
при сбое — что доживает до commit, а что откатывается.

## Что в теме

- атомарность: `@Transactional` откатывает уже записанные UPDATE при
  unchecked-исключении;
- `noRollbackFor`: выбранный RuntimeException не откатывает
  транзакцию;
- `Propagation.REQUIRES_NEW`: собственная транзакция, вызывающая — на
  паузе; commit внутренней переживает rollback внешней;
- `Propagation.MANDATORY`: метод обязан выполняться внутри транзакции
  вызывающего, иначе — `IllegalTransactionStateException`;
- `Propagation.REQUIRED` (дефолт): присоединение к транзакции
  вызывающего — изменение умирает вместе с ней (контраст к 03);
- `rollbackFor`: checked-исключение откатывает транзакцию только при
  явном объявлении (по умолчанию checked — коммит);
- `TransactionTemplate`: программная граница; commit внутри шаблона
  переживает последующее исключение;
- `readOnly = true`: подсказка «только чтение» — что она делает и чего
  не делает в стеке Hibernate 6 + H2;
- изолированная внутренняя граница: «откат только внутренней части»
  (аналог NESTED) через внутреннюю REQUIRES_NEW-транзакцию;
- пакет операций, где каждая операция — собственная транзакция.

## Важные факты стека (проверены экспериментально)

- `Product.stockQuantity` ограничен `@PositiveOrZero` (тема 12):
  записать отрицательный остаток в БД нельзя — Bean Validation сработает
  на flush раньше вашего бизнес-проверки. Поэтому проверки «не уйти в
  минус» делаются в памяти ДО flush.
- `Propagation.NESTED` в этом стеке недоступен: `JpaTransactionManager`
  (JpaDialect) не поддерживает savepoints, Hibernate 6 не имеет
  `Session.savepoint`. Семантика «откат только внутренней части»
  (задание 09) достигается внутренней REQUIRES_NEW-транзакцией.
- `readOnly` — оптимизация, а не guard: dirty-UPDATE managed-сущности в
  readOnly-транзакции молча не записывается (без исключения), а
  `persist` в ней работает как обычно.

## Стартовый код (уже в проекте)

- сервис-скелет `com.example.jpalab.service.ProductService`
  (бин `productService`): существующие методы не трогайте, новые
  добавляйте в этот же класс;
- сущности `Product`, `Category`, `Order` и репозитории — как в
  предыдущих темах;
- автоконфигурированный `PlatformTransactionManager`
  (`JpaTransactionManager`), H2, уровень изоляции READ COMMITTED.

Создаёт студент (артефакты заданий):

- `com.example.jpalab.exception.PriceSyncException`
  (`extends RuntimeException`) — задание 02;
- `com.example.jpalab.exception.OverstockLimitException`
  (`extends Exception`, checked) — задание 06.

## Тесты

Все тесты темы — `@SpringBootTest` и НЕ транзакционные: реальные
транзакции в тестах открываются только через `TransactionTemplate`
(создаётся из автоинжектнутого `PlatformTransactionManager`). Проверка
результатов — фактическое состояние БД (нативные SELECT), а не
состояние сессии.

## Границы темы

- Только атрибуты `@Transactional` (`propagation`, `readOnly`,
  `rollbackFor`, `noRollbackFor`) + `TransactionTemplate` + правила
  отката по типу исключения.
- Без уровней изоляции (у H2 только READ COMMITTED — тема 22),
  без `@Version` (тема 12), без `@Lock` (тема 23), без flush/
  ActionQueue как темы (не отдельная тема курса), без кэшей
  (темы 24–25).
- `flush()` в методах — инструмент, а не тема: его используют там, где
  поведение без него неоднозначно.

## Задания

| # | Файл | Артефакт | Сложность |
|---|------|----------|-----------|
| 01 | [TASK-01.md](TASK-01.md) | `transferStock` — атомарность по умолчанию | 1 |
| 02 | [TASK-02.md](TASK-02.md) | `bulkUpdatePrices` + `noRollbackFor` | 2 |
| 03 | [TASK-03.md](TASK-03.md) | `createProductWithCategory` + REQUIRES_NEW | 3 |
| 04 | [TASK-04.md](TASK-04.md) | `moveProductToCategory` + MANDATORY | 4 |
| 05 | [TASK-05.md](TASK-05.md) | `updateProductPrice` + REQUIRED (дефолт) | 5 |
| 06 | [TASK-06.md](TASK-06.md) | `adjustStock` + `rollbackFor` (checked) | 6 |
| 07 | [TASK-07.md](TASK-07.md) | `transferStockCommittedFirst` + TransactionTemplate | 7 |
| 08 | [TASK-08.md](TASK-08.md) | `previewStockAfterPromo` + readOnly | 8 |
| 09 | [TASK-09.md](TASK-09.md) | `transferStockSafely` — изолированная внутренняя граница | 9 |
| 10 | [TASK-10.md](TASK-10.md) | `processStockTransfers` + `transferStockAtomically` | 10 |

Проверка каждого задания — точная команда в его файле.

Вся тема: `mvn -Dtest="com.example.jpalab.tasks.t19.TASK*Test"`.
