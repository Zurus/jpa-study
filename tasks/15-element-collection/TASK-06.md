# Тема 15, задание 06

Тема: Элементные коллекции (@ElementCollection)
Сложность: 6

## Что нужно сделать
Добавить в `ProductElementCollectionService` метод:

```java
public Set<Long> findProductIdsByAttribute(String key, String value) {
    // TODO: вернуть id всех продуктов, у которых Product.attributes содержит
    //      ИМЕННО эту пару (ключ == key И значение == value)
}
```

Совпадает только точная пара: ключ без нужного значения (и наоборот)
не подходит. Неизвестная пара — пустой набор.

## Как проверить задание
```bash
mvn -Dtest=com.example.jpalab.tasks.t15.TASK06Test#findProductIdsByAttributeKeyAndValue
```

## Критерий приёмки
- возвращаются id ровно тех продуктов, у которых есть пара `key -> value`;
- «ключ совпал, но значение другое» — не возвращается;
- неизвестная пара — пустой `Set`.

## Подсказка (без решения)
Путь `a.key`/`a.value` по элементной Map в Hibernate 6 недоступен;
ключ и значение элемента Map извлекаются функциями
`KEY(a)` и `VALUE(a)` (HQL Hibernate 6) после `join p.attributes a`.
