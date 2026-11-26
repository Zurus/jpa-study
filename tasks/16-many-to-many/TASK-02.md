# TASK-02. Удаление одной связи из владельца

Тема: @ManyToMany. Сложность: 2/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductTagService`:

```java
public void removeTagFromProduct(Long productId, Tag tag) {
    // TODO
}
```

Метод убирает связь продукта с тегом: строка пары исчезает из
`product_tag_link`, остальные связи продукта и тега не трогаются.
Удаление несуществующей связи — no-op, без исключений.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t16.TASK02Test#ownerSideRemoveDeletesJoinRowOnly
```

## Критерии

- строка `(product_id, tag_id)` удалена;
- другие связи того же продукта на месте;
- удаление несуществующей связи не бросает исключение и ничего не ломает.

## Подсказка

Опять всё делает владелец: `Set.remove(...)` на
`Product.productTags` managed-продукта. Сами delete по
`product_tag_link` писать не нужно — Hibernate снимет строку при diff.
