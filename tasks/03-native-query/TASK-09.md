# Задание 09. Результат native-запроса — managed-сущность
- Тема: Native queries
- Difficulty: 9
- Что нужно сделать: Добавьте в `ProductRepository` native-метод `List<Product> findNativeById(@Param("id") Long id)`: `@Query(nativeQuery = true)`, возвращающий товар по id в виде сущности (`SELECT * FROM products WHERE id = :id`). Заранее созданный тест (TASK09Test) использует его, чтобы доказать: сущности из native-запроса попадают в persistence context — результат идентичен результату `em.find()` (одна и та же инстанция), а изменение такой сущности и `flush` порождают UPDATE.
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Query(nativeQuery = true) — SELECT * FROM products WHERE id = :id
List<Product> findNativeById(@Param("id") Long id);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t03.TASK09Test#nativeResultsAreManaged
```
- Критерий приёмки: Тест: результат вашего native-запроса и `em.find` — одна инстанция (`isSameAs`); `em.contains` для результата — true; после изменения цены и `flush` в SQL-логе виден `update products ... where id = ?`, повторный native-SELECT показывает новую цену; в комментарии зафиксировано: native-результат проходит через тот же L1-кэш.
- Подсказка (не решение): native-запрос на сущность не «обходит» persistence context — он питает его.
