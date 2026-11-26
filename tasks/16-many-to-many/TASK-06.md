# TASK-06. Поиск продуктов по имени тега

Тема: @ManyToMany. Сложность: 6/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductTagService`:

```java
public Set<Long> findProductIdsByTagName(String tagName) {
    // TODO
}
```

Метод возвращает id всех продуктов, связанных с тегом, имя которого
равно `tagName` (точное совпадение). Продукты без тегов и теги с
другими именами в результат не попадают.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t16.TASK06Test#findProductIdsByTagNameReturnsOnlyLinked
```

## Критерии

- для имени тега — ровно те продукты, что с ним связаны, каждый один раз;
- для имени, которого нет, — пустое множество;
- продукты без тегов никогда не попадают в результат.

## Подсказка

Один JPQL-запрос: join от `Product` по `p.productTags` и фильтр по
`name` тега. `distinct` защищает от дублей при joins.
