# TASK-10. Сравнение стратегий по стоимости persist

Тема: Entity Inheritance. Сложность: 10/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductHierarchyService`:

```java
public Map<String, Integer> persistStatementCounts() {
    // TODO
}
```

Метод по очереди persist-ит (с flush) по одной новой сущности
из каждой иерархии — `DigitalProduct` (SINGLE_TABLE), `Car`
(JOINED), `PdfDocument` (TABLE_PER_CLASS) — и замеряет число
prepared statement через Hibernate `Statistics`.

Возвращаемый мапа:

- `"singleTable"` → число prepared statement при persist цифрового товара;
- `"joined"` → то же для `Car`;
- `"tablePerClass"` → то же для `PdfDocument`.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t17.TASK10Test#persistStatementCountsPerStrategy
```

## Критерии

- `singleTable = 1` (один INSERT в общую таблицу);
- `joined = 2` (INSERT в корень + INSERT в подкласс);
- `tablePerClass = 2` (next value последовательности + INSERT).

## Подсказка

`Statistics` доступен через
`entityManager.unwrap(Session.class).getSessionFactory().getStatistics()`;
перед каждым замером сбрасывайте счётчики (`statistics.clear()`).
`TABLE_PER_CLASS` с `SEQUENCE` добавляет один prepared statement
на получение значения последовательности — учтите это в замере.
