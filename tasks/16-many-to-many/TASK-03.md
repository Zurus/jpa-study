# TASK-03. Двунаправленное связывание: обе стороны видят связь

Тема: @ManyToMany. Сложность: 3/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductTagService`:

```java
public void linkBothSides(Long productId, Tag tag) {
    // TODO
}
```

Метод устанавливает связь «в обе стороны»: после вызова в том же
persistence context и `Product.productTags` содержит тег, и
`Tag.products` содержит продукт.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t16.TASK03Test#linkBothSidesVisibleFromBothDirections
```

## Критерии

- в `product_tag_link` строка пары есть;
- в том же persistence context `p.getProductTags()` содержит тег;
- в том же persistence context `t.getProducts()` содержит продукт;
- после `clear()` связь видна с обеих сторон и из БД.

## Подсказка

Изменение только владельца (`productTags.add`) корректно для БД, но
обратная сторона в памяти остаётся устаревшей — Hibernate
двунаправленные коллекции **не синхронизирует автоматически**.
Согласуйте обе стороны сами.
