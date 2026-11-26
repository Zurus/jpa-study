# TASK-07. Загрузка всех продуктов с тегами за один запрос (JOIN FETCH)

Тема: @ManyToMany. Сложность: 7/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductTagService`:

```java
public List<Product> findAllWithTags() {
    // TODO
}
```

Метод возвращает **все** продукты (включая продукты без тегов) так,
что коллекция `productTags` каждого продукта уже инициализирована
(можно читать без дополнительных запросов).

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t16.TASK07Test#findAllWithTagsUsesSingleFetchQuery
```

## Критерии

- в списке все продукты БД (и с тегами, и без);
- чтение `p.getProductTags()` у каждого продукта не рождает
  дополнительных SELECT;
- всего выполнено ровно **1** SQL-запрос (проверяется через
  `Statistics` Hibernate).

## Подсказка

Наивная `findAll()` + ленивые коллекции даёт 1 + N запросов (N+1).
Решение — `JOIN FETCH` по коллекции `productTags` в одном JPQL-запросе.
Следите за типом join: обычный `join fetch` молча выбросит продукты
**без** тегов — а они в результате обязаны быть.
