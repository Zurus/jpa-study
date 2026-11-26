# Тема 15, задание 03

Тема: Элементные коллекции (@ElementCollection)
Сложность: 3

## Что нужно сделать
Добавить в `ProductElementCollectionService` метод:

```java
public void setAttribute(Long productId, String key, String value) {
    // TODO: загрузить продукт по id и записать пару (key, value) в Product.attributes
}
```

Пара попадает в таблицу `product_attributes` как строка
(`attr_key` = ключ, `attr_value` = значение).
Повторная запись того же ключа ЗАМЕНЯЕТ значение, а не дублирует строку.

## Как проверить задание
```bash
mvn -Dtest=com.example.jpalab.tasks.t15.TASK03Test#setAttributeStoresKeyValue
```

## Критерий приёмки
- новая пара появляется в `product_attributes` (ключ и значение по своим колонкам);
- разные ключи — разные строки;
- повторная запись того же ключа: строка одна, значение — новое.

## Подсказка (без решения)
Ключ Map хранится в отдельной колонке благодаря `@MapKeyColumn`
(см. маппинг поля `Product.attributes`). Операция над Map-ом — одна строка кода.
