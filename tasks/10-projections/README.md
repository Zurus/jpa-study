# Тема 10 — Проекции

**Сложность:** 1–10 · **Заданий:** 10 · **Тестов:** 10 (по одному на задание, пакет `com.example.jpalab.tasks.t10`)

Проекция — возврат из запроса только нужных данных, а не полных сущностей:
скалярные поля, DTO, кортежи, поля из JOIN. Тема раскрывает четыре формы
проекций — open interface, record, класс с конструктором, скалярные поля —
и комбинации проекций с JOIN / LEFT JOIN.

## Почему это важно
- **Производительность**: меньше данных в памяти и в SQL-выборе.
- **Безопасность**: DTO не содержат чужих полей и не попадают в граф состояния.
- **Контроль формы данных**: interface (прокси), record (value-семантика), класс
  (конструктор), `Object[]` (позиционные столбцы) — четыре разных контракта.

## Матрица заданий

| # | Задание | Форма проекции | Метод в `ProductRepository` | Сложность |
|---|---|---|---|---|
| 01 | Скалярный кортеж (name, price), цена desc | `List<Object[]>` | `namesAndPricesAbove(BigDecimal)` | 1 |
| 02 | Open interface через @Query (существующий `ProductSummary`) | интерфейс | `summariesInStockAtLeast(int)` | 2 |
| 03 | Record через конструкторное выражение (существующий `ProductNamePrice`) | record | `cheapestInStock(int)` | 3 |
| 04 | Класс с 5-конструктором, поле из JOIN (`category.id`) (существующий `ProductDto`) | класс | `fullDtosForPricedAtLeast(BigDecimal)` | 4 |
| 05 | Проекция с JOIN: товар + имя категории (новый интерфейс) | интерфейс | `withCategoryName(String)` | 5 |
| 06 | JOIN + сортировка по полю категории (новый record) | record | `pricedInCategories(BigDecimal)` | 6 |
| 07 | LEFT JOIN: товары без категории (null в проекции, новый интерфейс) | интерфейс | `namesWithOptionalCategory()` | 7 |
| 08 | Ловушка: порядок аргументов конструкторного выражения (новый record) | record | `priceStockNameForPricedBelow(BigDecimal)` | 8 |
| 09 | JOIN + фильтр по категории + BETWEEN, 4 поля (новый интерфейс) | интерфейс | `categoryProductPrices(String, BigDecimal, BigDecimal)` | 9 |
| 10 | Один запрос в двух формах: record против нового интерфейса | record + интерфейс | `namePricePairsForPricedAtLeast(BigDecimal)`, `namePricePairsAsInterface(BigDecimal)` | 10 |

Новые DTO (задания 05, 06, 07, 08, 09, 10) студент создаёт сам в пакете
`com.example.jpalab.dto` — точные имена, поля и getter'ы заданы в тексте
заданий и проверяются тестами. Существующие DTO (`ProductSummary`,
`ProductNamePrice`, `ProductDto`) используются в заданиях 02–04.

## Проверка
```bash
mvn test
```
Или отдельный тест:
```bash
mvn -Dtest=com.example.jpalab.tasks.t10.TASK01Test#scalarTupleProjectionOrderByPriceDesc
```

Каждый тест краснеет до реализации и зеленеет после правильной реализации.
Решения в репозиторий не добавляются.
