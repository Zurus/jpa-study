# Тема 15, задание 05

Тема: Элементные коллекции (@ElementCollection)
Сложность: 5

## Что нужно сделать
Добавить в `ProductElementCollectionService` метод:

```java
public Set<Long> findProductIdsByTag(String tag) {
    // TODO: вернуть id всех продуктов, у которых в Product.tags есть этот тег
}
```

Только точное совпадение тега (без «contains»). Неизвестный тег — пустой набор.

## Как проверить задание
```bash
mvn -Dtest=com.example.jpalab.tasks.t15.TASK05Test#findProductIdsByTagReturnsOnlyMatching
```

## Критерий приёмки
- возвращаются id ровно тех продуктов, чей `tags` содержит тег;
- продукт без тегов не возвращается ни по какому запросу;
- неизвестный тег — пустой `Set`.

## Подсказка (без решения)
JPQL/HQL: можно сделать `join` по элементной коллекции и сравнить её
элемент с параметром (аналог `join p.category` из темы 05, только
связываемая сторона — Set, а не ассоциация).
