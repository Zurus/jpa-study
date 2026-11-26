# Тема 14: Criteria API: программные запросы

## Чему учит тема

- Программная сборка запроса: `CriteriaBuilder`, `CriteriaQuery<T>`, `Root`,
  `Path`, `Predicate`, `Join` — вместо строкового JPQL/derived.
- Динамические WHERE-условия: предикаты добавляются условно и в цикле.
- Выборка `Path` вместо целой сущности (`CriteriaQuery<String>`).
- `Join` (inner) и `LEFT JOIN` + NULL-семантика (товар без категории).
- Сортировка через `CriteriaQuery.orderBy` (тай-брейк при равных значениях).
- Агрегатные функции (`cb.sum`, `cb.avg`) — включая NULL по пустой группе.
- Подзапросы (`CriteriaQuery.subquery`) для «категории с дорогим товаром».
- GROUP BY / HAVING в программной форме (аналог запроса темы 04 —
  разрешённое исключение, акцент на API).

Границы темы: Criteria API **напрямую**. Без Specification (тема 13),
без Querydsl. Статические фильтры тем 01–04 и GROUP BY/HAVING темы 04
разрешено повторять именно в форме Criteria API; чистая пагинация — тема 08.

## Задания

| # | Задание | Difficulty |
|---|---------|------------|
| 01 | Первый программный запрос: `eq` по имени (`findByName`) | 1 |
| 02 | Диапазон цены: `and` из двух сравнений, границы включаются (`findPriceBetween`) | 2 |
| 03 | Выборка `Path`: `CriteriaQuery<String>`, имена вместо сущностей (`findNamesByStockAtLeast`) | 3 |
| 04 | `Join` по many-to-one: товары категории, inner-join исключает NULL (`findByCategoryName`) | 4 |
| 05 | `LEFT JOIN` + динамический WHERE: null — все, включая товар без категории (`findByCategoryNameLeftJoin`) | 5 |
| 06 | Сортировка `price DESC, name ASC` (тай-брейк) (`findSortedPriceDescNameAsc`) | 6 |
| 07 | Динамический WHERE: список имён + минимальная цена + флаг «в наличии» (`findDynamic`) | 7 |
| 08 | Агрегат `SUM(price)` по категории; пустая группа — null (`sumPricesInCategory`) | 8 |
| 09 | Подзапрос: товары из категорий, где есть товар `>= threshold` (`findInCategoriesWithPriceAtLeast`) | 9 |
| 10 | GROUP BY + HAVING (`AVG >= min`), orderBy по группе (`findCategoryNamesWithAveragePriceAtLeast`) | 10 |

## Диапазон сложности

1–3 — базовые объекты Criteria (Root/Path/предикат); 4–7 — Join, LEFT JOIN,
сортировка, программная сборка динамических условий; 8–10 — агрегаты,
подзапросы, GROUP BY/HAVING с NULL-семантикой.

## Как проверить задание

Каждое задание имеет заранее созданный фиксированный тест
(`com.example.jpalab.tasks.t14.TASKXXTest`, `@DataJpaTest` +
`@Import(ProductCriteriaService.class)`). До реализации — красный, после —
зелёный. Команда проверки указана в каждом `TASK-XX.md`:

```bash
mvn -Dtest=com.example.jpalab.tasks.t14.TASK01Test#criteriaFindByNameIsExactMatch
```

Полный прогон: `mvn test`.

Артефакт темы — методы сервиса `com.example.jpalab.service.ProductCriteriaService`
(скелет сервиса с `EntityManager` уже в стартовом коде; методы добавляет студент).
Тесты вызывают методы через `TaskTestSupport.invoke`.

Решения в этой папке не раскрываются.
