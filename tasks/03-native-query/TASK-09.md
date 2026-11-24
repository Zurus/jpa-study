# Задание 09. Результат native-запроса — managed-сущность
- Тема: Native queries
- Difficulty: 9
- Что нужно сделать: Тест создан заранее (TASK09Test): native-запрос + `EntityManager` доказывают, что сущности из native-запроса попадают в persistence context: результат native-SELECT идентичен результату `em.find()` (одна и та же инстанция), а изменение такой сущности и `flush` порождают UPDATE.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t03.TASK09Test
// 1. сохранение товара (цена 100)
// 2. native: SELECT * FROM products WHERE id = :id -> List<Product>
// 3. em.find(Product.class, id) -> сравнение isSameAs, em.contains
// 4. изменение цены, flush, UPDATE в SQL-логе, новое значение в БД
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t03.TASK09Test#nativeResultsAreManaged
```
- Критерий приёмки: Тест: результат native-запроса и `em.find` — одна инстанция (`isSameAs`); `em.contains` для результата native-запроса — true; после изменения цены и `flush` в SQL-логе виден `update products ... where id = ?` (с `version`), повторный native-SELECT показывает новую цену; в комментарии зафиксировано: native-результат проходит через тот же L1-кэш.
- Подсказка (не решение): native-запрос на сущность не «обходит» persistence context — он питает его.
