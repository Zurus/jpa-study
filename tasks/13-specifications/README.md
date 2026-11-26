# Тема 13: Specification: динамические предикаты

## Чему учит тема

- `Specification<Product>` — переиспользуемый композиционный предикат (интерфейс
  `org.springframework.data.jpa.domain.Specification`): создаётся один раз,
  комбинируется, применяется через `JpaSpecificationExecutor`.
- `JpaSpecificationExecutor`: `findAll(spec)`, `findAll(spec, pageable)`, `count(spec)` —
  уже добавлен в `ProductRepository` (стартовый код).
- Комбинаторы: `and`, `or`, `not`, `Specification.where` — из простых предикатов
  собирается динамический фильтр.
- Динамические фильтры: аргумент `null` — предикат «всегда верен», фильтр «выключен».
- Предикаты по свойству-пути через ассоциацию (`category.name`) — implicit-join.
- Приоритет `and`/`or`: группировка должна быть явной.
- Комбинация Specification + `Pageable` (страницы по динамическому фильтру).

Границы темы: только Specification и `JpaSpecificationExecutor`. Без прямого Criteria API,
без Querydsl. Статические фильтры — темы 01–04, чистая пагинация — тема 08
(здесь — только в сочетании со Specification).

## Задания

| # | Задание | Difficulty |
|---|---------|------------|
| 01 | Первый Specification: точное совпадение по имени (`byName`) | 1 |
| 02 | Comparison-предикат: `price >= minPrice`, граница включается (`byPriceAtLeast`) | 2 |
| 03 | Динамический фильтр: null — все строки, иначе case-insensitive contains (`byNameContainingIgnoreCaseOptional`) | 3 |
| 04 | Композиция переиспользуемых спецификаций через `and` (`inCategoryWithPriceAtLeast`) | 4 |
| 05 | Комбинатор `or`: имя ИЛИ остаток (`nameContainingOrStockAtMost`) | 5 |
| 06 | Отрицание через `not` (`withoutNameContaining`) | 6 |
| 07 | Динамический поиск: три опциональных фильтра через `and` (`searchByNameAndPriceAndStock`) | 7 |
| 08 | Предикат по `category.name` + Specification и Pageable (`byCategoryNameContaining`) | 8 |
| 09 | Приоритет `and`/`or`: явная группировка OR внутри условия по имени (`namedAndPricedOrScarce`) | 9 |
| 10 | Полная динамическая спецификация: открытый диапазон цены, категория, остаток (`search`) | 10 |

## Диапазон сложности

1–3 — один предикат; 4–7 — комбинации (and/or/not, переиспользование, опциональные фильтры);
8–10 — свойство-путь через ассоциацию + Pageable, приоритет комбинаторов, NULL-категория.

## Как проверить задание

Каждое задание имеет заранее созданный фиксированный тест
(`com.example.jpalab.tasks.t13.TASKXXTest`). До реализации — красный, после — зелёный.
Команда проверки указана в каждом `TASK-XX.md`:

```bash
mvn -Dtest=com.example.jpalab.tasks.t13.TASK01Test#byNameFindsExactMatch
```

Полный прогон: `mvn test`.

Артефакт темы — класс `com.example.jpalab.repository.ProductSpecifications`
(публичный, публичный no-arg конструктор, статические методы, возвращающие
`Specification<Product>`). Тесты находят класс и методы рефлексивно
(`TaskTestSupport`), поэтому модуль компилируется до реализации.

Решения в этой папке не раскрываются.
