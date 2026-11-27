# TASK-09. Ловушка NULL в полиморфной таблице

Тема: Entity Inheritance. Сложность: 9/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductHierarchyService`:

```java
public Set<Long> findDigitalProductIdsWithoutDownloadUrl() {
    // TODO
}
```

Метод возвращает `id` только **цифровых** товаров, у которых
`downloadUrl = null`.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t17.TASK09Test#findDigitalProductIdsWithoutDownloadUrlIgnoresPhysicalRows
```

## Критерии

- в базе: цифровой со ссылкой, цифровой без ссылки,
  физический товар;
- результат — ровно `id` цифрового без ссылки;
- `id` физического товара **не входит** в результат
  (у него `download_url` тоже NULL, но он не цифровой).

## Подсказка

Наивное «`WHERE download_url IS NULL`» по `products_base`
вернёт и физические товары — в полиморфной таблице колонки
чужих типов равны NULL. Фильтруйте по типу: запрос по конкретному
подклассу сам учитывает дискриминатор.
