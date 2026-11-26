# Тема 15, задание 04

Тема: Элементные коллекции (@ElementCollection)
Сложность: 4

## Что нужно сделать
Добавить в `ProductElementCollectionService` метод:

```java
public void removeAttribute(Long productId, String key) {
    // TODO: загрузить продукт по id и удалить пару из Product.attributes по ключу
}
```

Исчезает ровно строка этого ключа; остальные пары не трогаются.
Удаление отсутствующего ключа — безопасный no-op.

## Как проверить задание
```bash
mvn -Dtest=com.example.jpalab.tasks.t15.TASK04Test#removeAttributeDeletesRowOnly
```

## Критерий приёмки
- строка с удалённым ключом исчезает из `product_attributes`;
- остальные пары продукта остаются;
- удаление несуществующего ключа не ломает коллекцию и не бросает исключение.

## Подсказка (без решения)
Аналог задания 02, но для Map: удаление идёт по ключу.
