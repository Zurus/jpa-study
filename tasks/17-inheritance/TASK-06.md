# TASK-06. Запрос по подклассу в полиморфной таблице

Тема: Entity Inheritance. Сложность: 6/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductHierarchyService`:

```java
public Set<Long> findPhysicalProductIdsByWeightAtLeast(Double minWeight) {
    // TODO
}
```

Метод возвращает `id` **только физических** товаров с весом не
меньше `minWeight`.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t17.TASK06Test#findPhysicalProductIdsByWeightAtLeastNeverReturnsDigital
```

## Критерии

- при пороге 2.0 возвращается только «тяжёлый» физический товар;
- при пороге 0.5 — оба физических;
- `id` цифровых товаров никогда не входят в результат (в
  `products_base` у них `weight = NULL`).

## Подсказка

Запрос по конкретному подклассу — Hibernate подставит условие по
дискриминатору сам. Проверяйте, что фильтр по весу не тянет
цифровые строки из общей таблицы.
