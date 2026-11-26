# Тема 15, задание 10

Тема: Элементные коллекции (@ElementCollection)
Сложность: 10

## Что нужно сделать
Добавить в `ProductElementCollectionService` метод:

```java
public void persistProductWithPreparedCollection(Product product) {
    // TODO: сохранить transient-продукт, у которого ДО persist уже
    //      заполнены Product.tags и Product.attributes
}
```

После вызова: у продукта есть `id`, а строки обеих элементных коллекций
присутствуют в их таблицах. Отдельно обрабатывать строки коллекций
не нужно — разберитесь, что делает `persist` каскадно по умолчанию
для `@ElementCollection`.

## Как проверить задание
```bash
mvn -Dtest=com.example.jpalab.tasks.t15.TASK10Test#persistProductWithPreparedCollection
```

## Критерий приёмки
- `product.getId()` не null после вызова;
- в `product_tags` ровно строки перед persist;
- в `product_attributes` ровно пары перед persist.

## Подсказка (без решения)
У `@ElementCollection` есть дефолтные значения `cascade` (см.
JPA-спецификация): подумайте, какие именно операции по умолчанию
каскадируются от owner к элементной коллекции.
