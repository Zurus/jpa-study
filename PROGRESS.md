# PROGRESS.md — JPA Lab

- Дата: 2026-07-11 (повторный аудит по чек-листу + очистка решений из tasks/).
- Команда: `mvn test` (из корня `jpa-lab`).

## Аудит (состояние на 2026-07-11)

- `tasks/`: темы `01`–`08`, **80 TASK-файлов** (10 в каждой). Тем `09`–`12` в `tasks/` нет.
- Тесты: **80 тестовых классов** `TASK01Test`…`TASK10Test` в пакетах `com.example.jpalab.tasks.t01`…`t08` + 34 example-теста.
- `answers/` отсутствует и не создаётся. Решений в `src/` нет.
- `TaskTestSupport.invoke(...)`: рефлексивный вызов целевого метода; до реализации — `AssertionError("Задание не реализовано: ...")`.

## Повторная проверка соответствия (чек-лист, 2026-07-11)

| # | Пункт | Результат |
|---|---|---|
| 1 | В каждой теме ровно 10 `TASK-01.md`…`TASK-10.md` | **OK**: 8/8 тем по 10 файлов |
| 2 | На каждое задание один тестовый класс | **OK**: 80 классов, имя `TASK<NN>Test` соответствует номеру |
| 3 | В задании указан корректный тест (`mvn -Dtest=FQCN#method`) | **OK**: 80/80 — FQCN существует, пакет совпадает, метод в классе есть |
| 4 | Тест проверяет именно требование задания | **OK**: целевые методы совпадают с «Что нужно сделать»; 39 зелёных тестов проверены опытом; 41 красный — через `invoke` по фиксированному имени |
| 5 | `difficulty` 1–10 | **OK**: 80/80 |
| 6 | Нет дублей между темами | **OK**: единственное пересечение имён — `findByPriceGreaterThan` (тема 01: `List<Product>, BigDecimal`; тема 08: `Page<Product>, BigDecimal + Pageable`) — разные перегрузки, разные задачи |
| 7 | Нет решений внутри `tasks/` | **БЫЛО НАРУШЕНИЕ → исправлено**: в 29 файлах (02×8, 03×8, 04×9, 07×3, 08×1) блок «Стартовый код» содержал полное `@Query`/`@EntityGraph` с текстом запроса — готовое решение. Заменено на `// TODO: ...` + требуемая сигнатура метода (контракт с тестом сохранён). Демонстрационные задачи (02/08, 02/10, 03/07, 03/09, 04/09, 07/09) содержат закомментированные иллюстративные примеры — это материал задачи «наблюдайте и объясните», не решение |

## Итог `mvn test` (после правки темы 02)

```
Tests run: 114, Failures: 42, Errors: 0, Skipped: 0
```

- 34 example-теста — все зелёные.
- 38 task-тестов — зелёные (задачи, не требующие кода студента).
- 42 task-теста — красные **как задумано**: `AssertionError("Задание не реализовано: ...")` (0 неожиданных сбоев).

## Тема 01 (Derived queries) — проверка 10/10 (2026-07-11)

- Все 10 тестовых классов существуют: `src/test/java/com/example/jpalab/tasks/01-derived-queries/TASK01Test`…`TASK10Test`.
- **Недостающих тестов нет — ничего не создавалось.**
- Каждое задание ↔ тест сверено: `@DataJpaTest` + AssertJ, минимальные данные через `save`, целевой метод — через `TaskTestSupport.invoke` (без `@Query`, только derived queries, как требует тема):
  - 01 `findByStockQuantityGreaterThan` · 02 `findByNameIgnoreCase` · 03 `findTop5ByOrderByPriceDesc` · 04 `findByNameContainingIgnoreCaseAndPriceBetween` · 05 `findFirstByCategoryName` · 06 `findByNameContainingOrPriceLessThanEqual` · 07 `findByNameLikeIgnoreCase(Pageable)` · 08 `findByCategoryIsNull` + `findByCategoryIsNotNull` · 09 `findByPriceGreaterThan` · 10 `findByPriceGreaterThanAndPriceLessThanEqual`.
- `mvn test`: все 10 красные **как задумано** — `AssertionError("Задание не реализовано: ...")`; неожиданных сбоев нет. Убран старый отчёт `ScratchDbStateTest` из `target/surefire-reports` (класс давно удалён, в сборку не попадает).

## Тема 02 (@Query JPQL) — проверка 10/10 (2026-07-11)

- Все 10 тестовых классов существуют: `src/test/java/com/example/jpalab/tasks/02-jpql-query/TASK01Test`…`TASK10Test`; недостающих не было.
- **Исправлено нарушение спецификации:** старое задание 06 было «Constructor expression (DTO-проекция)» по существующему методу `namePricesAtLeast` — а в спеце тема 02: «Не включать constructor expression — это тема 10». Задание 06 и его тест заменены на соответствующую теме задачу «Выборка одного поля»:
  - новый метод-цель: `List<String> findNamesByPriceBetween(@Param("from") BigDecimal from, @Param("to") BigDecimal to)` (в репозитории отсутствует — тест красный до реализации);
  - `TASK06Test` переписан: `@DataJpaTest`, 4 товара, `TaskTestSupport.invoke`, проверка границ диапазона (оба конца исключены) и ORDER BY по имени; AssertJ.
  - `tasks/02-jpql-query/README.md`: строка «Чему учит» и строка таблицы обновлены (constructor expression убран из темы).
- Остальные 9 задач сверены: `@DataJpaTest` + AssertJ, минимальные данные, целевой метод через `invoke` (01 `findPricedAbove`, 02 `findPricedAboveSortedDeterministic`, 03 `findProductsOfCategory`, 04 `searchByNameFragmentIgnoreCase`, 05 `idNamePriceAbove`, 07 `findPageByPriceBetween`, 09 `findProductsInCategoriesLike`) либо динамический JPQL через `EntityManager` в показательных задачах (08 — колонка vs свойство, 10 — L1-кэш; обе зелёные, код теста — материал наблюдения).
- Итог по теме: 8 красных (ожид.) + 2 зелёных (08, 10).

## Тема 03 (@Query native SQL) — проверка 10/10 (2026-07-11)

- Все 10 тестовых классов существуют: `src/test/java/com/example/jpalab/tasks/03-native-query/TASK01Test`…`TASK10Test`; **недостающих нет — ничего не создавалось**.
- Каждое задание ↔ тест сверено: `@DataJpaTest` + AssertJ, минимальные данные, целевой метод через `TaskTestSupport.invoke` либо динамический SQL/EntityManager в показательных задачах:
  - 01 `findNativePricedAbove` · 02 `findNativePricedAtLeastWithColumns` · 03 `nativeSearchByPriceAbove` (существующий метод, зелёный) · 04 `findNativeByCategoryName` · 05 `nativePricesByName` · 06 `findNativePage` · 08 `findNativeWithoutCategory` · 10 `findNativeAll`;
  - 07 «Ловушка: constructor expression в native не работает» и 09 «native-результат попадает в persistence context» — показательные (зелёные); задание 07 соответствует спеце: демонстрирует **ограничение** native SQL («difference from JPQL … ограничения»), а не реализацию constructor expression.
- Нарушений спецификации темы не найдено. Итог по теме: 7 красных (ожид.) + 3 зелёных (03, 07, 09).

## Текущая задача

Тема 03: проверена, 10/10 тестов на месте, новых не требовалось, нарушений спецы нет.

## Следующий шаг

Тема 04: проверить 10 заданий на готовые тесты (по аналогии). После — студент реализовывает методы: каждый красный тест станет зелёным. Проверка: команда из соответствующего `TASK-XX.md` (`mvn -Dtest=<FQCN>#<method>`) или полный `mvn test`.
