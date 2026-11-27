# TASK-08. readOnly: подсказка, а не guard

Тема: `@Transactional`: propagation и rollback. Сложность: 8/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductService`:

```java
@Transactional(readOnly = true)
public int previewStockAfterPromo(Long id, int promoDelta) {
    // TODO
}
```

Поведение: загрузить товар `id` и вернуть `stockQuantity + promoDelta`
(превью остатка «как будто прошла промо-операция»).

Требование: БД не меняется: ни UPDATE, ни новая строка. Метод —
чистое чтение.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t19.TASK08Test#readOnlyHintDoesNotPersistManagedChange
```

## Критерии

- склад 10, `promoDelta = 5`: метод возвращает 15;
- в БД склад остался 10;
- в таблице `products` не появилось новых строк.

## Подсказка

В стеке Hibernate 6 + H2 `readOnly` — оптимизация, а не guard: dirty-
UPDATE managed-сущности в readOnly-транзакции молча не записывается
(без исключения), а `persist` в ней работает как обычно. Поэтому
«только чтение» обеспечивает сам метод: управляемую сущность просто не
модифицируют, результат складывают в локальную переменную.
