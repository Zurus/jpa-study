# Задание 06. Batch fetching: default_batch_fetch_size
- Тема: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
- Difficulty: 6
- Что нужно сделать: Реализуйте в `ProductService` метод `List<String> allProductCategoryNames()` — загрузить ВСЕ товары и собрать строки вида `"<name> [<categoryName>]"` (категории — LAZY-прокси). Заранее созданный тест (TASK06Test) поднимает контекст с `spring.jpa.properties.hibernate.default_batch_fetch_size=2` и показывает: при 3 УНИКАЛЬНЫХ категориях запросы на прокси собираются в `IN`-батчи по 2 id — 2 SELECT вместо 3 отдельных (итого 3, а не 1 + 3).
- Стартовый код:
```java
// com/example/jpalab/service/ProductService.java

// TODO: findAll() + цикл: name + " [" + category.getName() + "]"
List<String> allProductCategoryNames();
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t07.TASK06Test#defaultBatchFetchSizeCollapsesProxyLoads
```
- Критерий приёмки: Тест: 6 товаров / 3 категории (3, 2, 1); ваш метод возвращает 6 строк; всего ровно 3 prepared statement (1 по products + 2 батча категорий: `where category_id in (?, ?)` и `in (?)`); в комментарии зафиксировано: batch size задан свойством контекста, а без него было бы 3 отдельных SELECT.
- Подсказка (не решение): `default_batch_fetch_size` собирает запросы прокси в `IN`-списки — N+1 сжимается в ceil(N/batchSize).
