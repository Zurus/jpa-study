# TASK-05. Удаление тега: продукты остаются (cascade = NONE)

Тема: @ManyToMany. Сложность: 5/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductTagService`:

```java
public void deleteTagKeepsProducts(Long tagId) {
    // TODO
}
```

Метод удаляет тег из БД. При этом продукты, которые были с ним
связаны, **должны остаться** (у `@ManyToMany` по умолчанию
`cascade = NONE`), а все строки связей этого тега из
`product_tag_link` должны исчезнуть.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t16.TASK05Test#deleteTagRemovesLinksButKeepsProducts
```

## Критерии

- `TagRepository.findById(tagId)` — пусто;
- в `product_tag_link` нет ни одной строки с этим `tag_id`;
- связанные продукты не удалены;
- связи этих продуктов с другими тегами не пострадали.

## Подсказка

Наивное `em.remove(tag)` падает на FK: строки в
`product_tag_link` **владеет Product** (`product_id` — FK из
join-таблицы), поэтому сначала нужно оборвать связи со стороны
владельца. А опираться на `tag.getProducts()` небезопасно: обратная
сторона в памяти может быть устаревшей (stale) — надёжнее найти
продукты через JPQL-join `Product p join p.productTags ...`.
