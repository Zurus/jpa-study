# TASK-06. rollbackFor: откат по checked-исключению

Тема: `@Transactional`: propagation и rollback. Сложность: 6/10.

## Что делать

1. Создать ПРОВЕРЯЕМОЕ (checked) исключение
   `com.example.jpalab.exception.OverstockLimitException`
   (`extends Exception`, НЕ `RuntimeException`).
2. Добавить метод в `com.example.jpalab.service.ProductService`:

```java
@Transactional(rollbackFor = OverstockLimitException.class)
public void adjustStock(Long productId, int delta) throws OverstockLimitException {
    // TODO
}
```

Поведение: приписать `delta` к складу товара, `flush`; если склад стал
больше 1000 — выбросить `OverstockLimitException`.

Требование: транзакция обязана откатиться, несмотря на то, что
исключение checked.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t19.TASK06Test#rollbackForRollsBackOnCheckedException
```

## Критерии

- склад 500, `delta = 600` (итог 1100 > 1000);
- метод выбрасывает `OverstockLimitException`; тест проверяет, что это
  именно checked-исключение (не `RuntimeException`, не `Error`);
- в БД склад остался 500 — UPDATE откатился;
- без `rollbackFor` тот же код закоммитил бы 1100.

## Подсказка

Правило отката по умолчанию: `RuntimeException` и `Error` — откат,
checked-исключения — коммит. `rollbackFor` расширяет список классов,
по которым происходит откат. (Симметричный инструмент —
`noRollbackFor` — задание 02.)
