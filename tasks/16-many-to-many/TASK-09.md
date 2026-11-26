# TASK-09. Общие теги двух продуктов (пересечение)

Тема: @ManyToMany. Сложность: 9/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductTagService`:

```java
public Set<String> findSharedTagNames(Long productId, Long otherProductId) {
    // TODO
}
```

Метод возвращает имена тегов, которыми обладают **оба** продукта
(пересечение их множеств). Результат симметричен: порядок аргументов
не влияет на ответ. Если общих тегов нет — пустое множество.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t16.TASK09Test#findSharedTagNamesIsIntersection
```

## Критерии

- для двух продуктов с одним общим тегом — ровно имя этого тега;
- объединение (union) или тег только одного из продуктов в ответе
  нет;
- `findSharedTagNames(a, b)` и `findSharedTagNames(b, a)` — одно
  и то же;
- без общих тегов — пусто.

## Подсказка

Пересечение удобно через подзапрос: теги первого продукта, которые
входят в множество тегов второго (`in (select p2.productTags ...)`).
`distinct` по именам обязатель при joins через обе коллекции.
