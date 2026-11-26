# PROGRESS.md — JPA Lab

- Дата: 2026-11-26. (1) Полный аудит и нормализация (12 тем × 10 заданий). (2) Создана тема 13 (Specification). (3) Создана тема 14 (Criteria API).
- Команда: `mvn test` (из корня `jpa-lab`).

## Аудит (что проверялось)

- Реальная структура репозитория, содержимое всех 120 `TASK-XX.md`, всех 120 тестовых классов, README тем и корневых документов.
- Базовый прогон `mvn test` ДО правок: `Tests run: 154, Failures: 75, Errors: 0` (120 task-тестов + 34 example-теста).
- Каждое задание ↔ тест: связка по каталогу темы (`tasks/NN-<тема>/TASK-XX.md` ↔ `src/test/java/com/example/jpalab/tasks/<NN>-<тема>/TASKXXTest.java`, пакет `tNN`), корректность `-Dtest=FQCN#method` (класс и метод существуют), что тест реально проверяет задание.
- Нарушения принципа «красный до реализации»: поиск тестов, зелёных на базовой линии (без кода студента).
- Раскрытие решений: в `TASK-XX.md` (блок «Стартовый код» с готовым `@Query`/кодом), в подсказках, в примерах/демо-тестах, в стартовом коде `src/main/java`.
- Дубли и границы тем (по специи): Specification/Criteria/Querydsl в 01–04; обычная фильтрация в 08; транзакционные сценарии в 11 vs плановая 19; `@Lock` в 12; L1/L2-кэш в 07; soft delete в 12.

## Найденные проблемы

1. **46 тестов были зелёны на базовой линии** (не требовали кода студента) — дефект «тест не проверяет задание»: 02×2 (08, 10), 03×3 (03, 07, 09), 04×1 (09), 05×10, 06×10, 07×6 (01, 02, 06, 08, 09, 10), 08×6 (01, 02, 03, 06, 07, 08), 09×3 (03, 07, 08), 12×4 (03, 04, 06, 09). Темы 05/06 вообще не имели артефакта студента («наблюдательные» тесты).
2. **14 legacy/example-тестов** (`src/test/java/.../repository/`, `.../lifecycle/`, в т.ч. `*ExampleTest`) — раскрывали решения, дублировали тематически и противоречили модели «один тест на задание».
3. **Решения в стартовом коде `src/main`**: `ProductRepository` содержал ~20 готовых `@Query`/`@EntityGraph`/`@Modifying`-методов (решения тем 01–09); `CategoryRepository` — 2 метода-решения темы 04 (`productStatsByCategory`, `categoryNamesWithAtLeastProducts`).
4. **Раскрытие решений в tasks/**: в темах 02/03/04/07/08 «Стартовый код» и «Подсказка» содержали готовые JPQL/выражения (в т.ч. полная формула `UPPER(p.name) LIKE UPPER(CONCAT('%',:fragment,'%'))` в 02/04).
5. **Устаревшая документация**: README тем 01–09 писали «создайте свой тест-класс `XxxTaskNNTest`» (старая модель); ссылки на удалённые example-тесты (11, 12); `PROJECT_SPEC.md` не содержал плановых тем 13–25 и границ тем; `PROGRESS.md` описывал состояние на 2026-07-11 (8 тем, 80 заданий).
6. **Ошибочные подписи**: 05/10 требовал REQUIRES_NEW (тест — в транзакции с откатом; метод должен вливаться в транзакцию вызывающего); в 05/09 в `-Dtest` указан несуществующий метод теста; тема 09: «Критерии приёмки» вместо «Критерий приёмки».
7. **Структура тестов**: каталоги тематические (`01-derived-queries`), пакеты `t01`…`t12` — имя класса `TASK01Test` уникально только в пределах темы; сверка по «голым» именам классов ненадёжна.

## Что исправлено

1. **Конвертация всех 46 «зелёных» тестов** в модель «артефакт студента»: тесты вызывают конкретные методы студента через `TaskTestSupport.invoke` (методов нет → `AssertionError` «Задание не реализовано»). Новые контракты:
   - Темы 05 и 06: 20 методов в `ProductLifecycleService` (каждый — фиксированная сигнатура; 05/10 и 06/10 — простой `@Transactional`, вливается в транзакцию теста).
   - Тема 07: `ProductService` (01, 02, 06, 08, 10) + `CategoryRepository.findAllWithProductsFetched()` (09, `@EntityGraph`).
   - Тема 08: методы `ProductRepository` с `Pageable`/`Slice` (01, 02, 03, 06, 08) + `ProductService.pageProductsForApi` (07).
   - Тема 09: `ProductRepository.zeroStockForPricedAtLeast/deleteZeroStockProducts/setStockForPricedAtLeast/raiseAllPricesBy` (03, 07, 08).
   - Тема 12: `ProductVersioningService.updateProductName/loadAndFlushProduct/staleSaveExceptionType` (03, 04, 06) + `ProductRepository.setStockForAll` (09).
   - Для `@DataJpaTest` + `@Service` добавлен `@Import(...)` (t05/t06 — `ProductLifecycleService`; t07/t08 — `ProductService`).
2. **Удалены 14 legacy/example-тестов** (каталоги `src/test/java/com/example/jpalab/repository/` и `.../lifecycle/`).
3. **Стартовый код очищен от решений**: `ProductRepository` — только `findByCategory` + `ProductSummary`; `CategoryRepository` — пустой (`JpaRepository<Category, Long>`); сервисы — только исходные `restock`/`updatePrice`.
4. **Перезаписаны 55 `TASK-XX.md`** в единый формат (темы 02×2, 03×3, 04×1, 05×10, 06×10, 07×6, 08×6, 09×3, 10×10, 12×4): «Что нужно сделать» без готовых запросов, «Стартовый код» = `// TODO` + сигнатура, точный `-Dtest=FQCN#method`, «Критерий приёмки» по тесту, подсказка без решения.
5. **README тем 01–09**: раздел «Как проверить задание» — модель заранее созданных тестов; ссылки на example-тесты и «создайте свой тест» убраны (11, 12 — аналогично; контракт сервиса 12 дополнен методами 03/04/06).
6. **Точечные правки**: подсказка 02/04 (готовая формула убрана), `REQUIRES_NEW` → `@Transactional` в 05/10, имя метода в 05/09, «Критерии приёмки» → «Критерий приёмки» (тема 09), 10/01 — устранена неоднозначность порядка.
7. **Документы**: `PROJECT_SPEC.md` — полная спецификация (технологии, сущности, принцип обучения, правила тестов, формат, правила генерации, темы 01–12, плановые 13–25, границы тем, скоуп); `PROGRESS.md` — этот файл.

## Состояние (после темы 14)

- Тем: **14** (01–14) × ровно 10 заданий = **140** `TASK-XX.md` (в каждой теме `TASK-01.md`…`TASK-10.md`).
- Тестовых классов: **140** (`TASK01Test`…`TASK10Test` в пакетах `com.example.jpalab.tasks.t01`…`t14`), по одному `@Test` на задание; каталоги тематические, сверка — по каталогу темы.
- Связка задание ↔ тест: **140/140** (темы 01–12 проверены скриптом при аудите; темы 13–14 — команды `-Dtest` вписаны по реальным именам методов тестов, сверено скриптом).
- Тесты, зелёные на базовой линии: **0** (темы 13–14 проверены: временные корректные реализации артефактов → 10/10 зелёных; реализации удалены, тесты снова красные; ловушки тем 13–14 проверены намеренно-неверными реализациями).
- `answers/`: отсутствует (не создаётся); markdown-решений нет.
- Стартовый код `src/main`: без готовых решений по заданиям; `ProductRepository` — `JpaRepository + JpaSpecificationExecutor` + `findByCategory` + `ProductSummary` (executor — стартовая инфраструкция темы 13, не решение); `ProductCriteriaService` — скелет (`@Service` + `@PersistenceContext EntityManager`) для темы 14.
- Дубли/границы: проверены (Specification/Criteria/Querydsl вне 01–04 и вне 13/14 напрямую; `@Lock`/soft delete/L2-кэш вне 12; N+1/пагинация без дублей; тема 13 — только Specification + JpaSpecificationExecutor; тема 14 — только Criteria API на прямую).

## Итог `mvn test` (после темы 14)

```
Tests run: 140, Failures: 140, Errors: 0, Skipped: 0
```

Все 140 тестов красны **как задумано** (нет кода студента; 10 тестов темы 13 падают с «Задание не реализовано: класс …ProductSpecifications не найден», 10 тестов темы 14 — с «Задание не реализовано: у целевого объекта нет метода …»); ошибок сборки и «неожиданных» сбоев нет.

## Тема 13: Specification: динамические предикаты (создана 2026-11-26)

**Созданные файлы:**
- `tasks/13-specifications/README.md` + `TASK-01.md`…`TASK-10.md` (формат спецификации).
- `src/test/java/com/example/jpalab/tasks/13-specifications/TASK01Test.java`…`TASK10Test.java` (пакет `com.example.jpalab.tasks.t13`, `@DataJpaTest`).

**Изменения в стартовом коде и инфраструктуре тестов:**
- `ProductRepository`: добавлен `JpaSpecificationExecutor<Product>` (стартовая инфраструкция, не решение).
- `TaskTestSupport`: добавлен `newInstance(className)` — рефлексивное создание класса-артефакта студента по FQCN (нет класса → `AssertionError` «Задание не реализовано»); тесты компилируются до реализации.

**Контракт (артефакт студента):** класс `com.example.jpalab.repository.ProductSpecifications` — публичный, публичный no-arg конструктор, статические методы, возвращающие `Specification<Product>`:
| # | Difficulty | Метод | Что проверяет |
|---|-----------|-------|----------------|
| 01 | 1 | `byName(String)` | eq-предикат (точное совпадение) |
| 02 | 2 | `byPriceAtLeast(BigDecimal)` | `>=`, граница включается |
| 03 | 3 | `byNameContainingIgnoreCaseOptional(String)` | null — все строки; иначе case-insensitive contains |
| 04 | 4 | `inCategoryWithPriceAtLeast(String, BigDecimal)` | `and` из переиспользуемых предикатов |
| 05 | 5 | `nameContainingOrStockAtMost(String, Integer)` | `or` из двух веток (каждая проверяется отдельно) |
| 06 | 6 | `withoutNameContaining(String)` | `not` (negation) |
| 07 | 7 | `searchByNameAndPriceAndStock(String, BigDecimal, Integer)` | 3 опциональных фильтра через `and` (null — выключен) |
| 08 | 8 | `byCategoryNameContaining(String)` | свойство-путь `category.name` + `findAll(spec, Pageable)`/`count(spec)` |
| 09 | 9 | `namedAndPricedOrScarce(String, BigDecimal, Integer)` | `name AND (price OR stock)` — ловушка приоритета: неверная группировка `(name AND price) OR stock` даёт лишний товар «Beta Two» (проверено: тест красный) |
| 10 | 10 | `search(BigDecimal, BigDecimal, String, Integer)` | полный динамический фильтр: открытый диапазон цены, NULL-категория (товар без категории теряется при всегда-join — проверено: тест красный), minStock |

**Эмпирическая валидация:** временная корректная реализация → 10/10 зелёных → реализация удалена; две ловушки (09 — неверная группировка, 10 — принудительный join) проверены намеренно-неверными реализациями (обе дают красный тест). В тестах и в `TASK-XX.md` нет готовых предикатов/выражений.

## Тема 14: Criteria API: программные запросы (создана 2026-11-26)

**Созданные файлы:**
- `tasks/14-criteria-api/README.md` + `TASK-01.md`…`TASK-10.md` (формат спецификации).
- `src/test/java/com/example/jpalab/tasks/14-criteria-api/TASK01Test.java`…`TASK10Test.java` (пакет `com.example.jpalab.tasks.t14`, `@DataJpaTest` + `@Import(ProductCriteriaService.class)`).

**Изменения в стартовом коде:**
- `src/main/java/com/example/jpalab/service/ProductCriteriaService.java` — новый сервис-скелет: `@Service` + `@PersistenceContext EntityManager` (стартовая инфраструктура, без методов-решений).

**Контракт (артефакт студента):** методы `com.example.jpalab.service.ProductCriteriaService` — запрос строится через `CriteriaBuilder`/`CriteriaQuery` и выполняется через `EntityManager`:
| # | Difficulty | Метод | Что проверяет |
|---|-----------|-------|----------------|
| 01 | 1 | `List<Product> findByName(String)` | `CriteriaQuery<Product>` + eq по name (точное совпадение) |
| 02 | 2 | `List<Product> findPriceBetween(BigDecimal, BigDecimal)` | два comparison-предиката через `cb.and`, границы включаются |
| 03 | 3 | `List<String> findNamesByStockAtLeast(int)` | выборка `Path` (скаляр `CriteriaQuery<String>`), `>=` включительно |
| 04 | 4 | `List<Product> findByCategoryName(String)` | `Join` по many-to-one (inner): товар без категории исключён |
| 05 | 5 | `List<Product> findByCategoryNameLeftJoin(String)` | `JoinType.LEFT` + null-фильтр: null — все, включая товар без категории (ловушка: inner join — проверено, тест красный) |
| 06 | 6 | `List<Product> findSortedPriceDescNameAsc(BigDecimal)` | `orderBy(цена DESC, имя ASC)`; данные против порядка вставки (ловушка: без тай-брейка — проверено, тест красный) |
| 07 | 7 | `List<Product> findDynamic(List<String>, BigDecimal, boolean)` | программная сборка WHERE: IN-имена (null/пустой — выключен), `price >=`, `stock > 0` |
| 08 | 8 | `BigDecimal sumPricesInCategory(String)` | `cb.sum` + join-категория; пустая группа → null |
| 09 | 9 | `List<Product> findInCategoriesWithPriceAtLeast(BigDecimal)` | подзапрос: все товары категорий, где есть товар `>= threshold` (ловушка: наивный `price >=` — проверено, тест красный) |
| 10 | 10 | `List<String> findCategoryNamesWithAveragePriceAtLeast(BigDecimal)` | GROUP BY + HAVING на `cb.avg` (тип `Expression<Double>`), orderBy по группе; NULL-категория без группы (ловушка: без HAVING — проверено, тест красный) |

**Эмпирическая валидация:** временная корректная реализация → 10/10 зелёных → реализация удалена (в `src/main` остался только скелет); 4 ловушки (05, 06, 09, 10) проверены намеренно-неверными реализациями — все дают красный тест. В тестах и в `TASK-XX.md` нет готовых запросов/выражений (подсказка — одна строка).

## План (следующие шаги)

1. Студент реализует задания по `TASK-XX.md` (репозитории / `ProductService` / `ProductLifecycleService` / `ProductVersioningService` / `ProductSpecifications` / `ProductCriteriaService` / сущности / DTO) — красные тесты становятся зелёными; проверка: команда из задания или полный `mvn test`.
2. Плановые темы 15–25 (из `PROJECT_SPEC.md`) — создавать только по отдельному запросу; на каждую — ровно 10 заданий по тем же правилам.
3. Коммит изменений (тесты, tasks/, docs, src/main).
