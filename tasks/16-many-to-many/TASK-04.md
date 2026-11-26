# TASK-04. Двунаправленный разрыв: обе стороны пусты

Тема: @ManyToMany. Сложность: 4/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductTagService`:

```java
public void unlinkBothSides(Long productId, Tag tag) {
    // TODO
}
```

Метод полностью разрывает связь: в том же persistence context обе
коллекции пусты (`Product.productTags` и `Tag.products`), строка в
`product_tag_link` удалена.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t16.TASK04Test#unlinkBothSidesEmptyFromBothDirections
```

## Критерии

- строки `(product_id, tag_id)` в `product_tag_link` нет;
- в том же persistence context `p.getProductTags()` не содержит тег;
- в том же persistence context `t.getProducts()` не содержит продукт;
- после `clear()` обе стороны пусты.

## Подсказка

Зеркальная задача TASK-03: удалите элемент и из владельца, и из
обратной стороны. Удаление только из владельца уберёт строку из БД, но
в памяти останется «висячая» ссылка.
