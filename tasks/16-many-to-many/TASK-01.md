# TASK-01. Добавление связи: владелец создаёт строку в join-таблице

Тема: @ManyToMany. Сложность: 1/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductTagService`:

```java
public void addTagToProduct(Long productId, Tag tag) {
    // TODO
}
```

Метод связывает продукт с тегом так, что в промежуточной таблице
`product_tag_link` появляется строка `(product_id, tag_id)`.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t16.TASK01Test#ownerSideAddCreatesJoinRow
```

## Критерии

- После вызова в `product_tag_link` ровно одна строка для пары
  (продукт, тег);
- несколько тегов — несколько строк;
- повторное добавление того же тега строку не дублирует
  (коллекция — `Set`).

## Подсказка

Промежуточную таблицу заполняет Hibernate сам: достаточно изменить
владелецскую коллекцию `Product.productTags` у managed-сущности и дать
коммиту/флешу произойти. Сам вставлять строки SQL-ом не нужно.
