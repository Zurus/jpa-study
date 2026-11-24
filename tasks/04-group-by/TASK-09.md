# Задание 09. Ловушка: алиас агрегата в HAVING
- Тема: GROUP BY / HAVING
- Difficulty: 9
- Что нужно сделать: Тест с ДИНАМИЧЕСКИМИ JPQL-запросами через `EntityManager` создан заранее (TASK09Test): (а) группировка по категориям с HAVING, ссылающимся на АЛИАС агрегата (например, `HAVING total > :min`, где `total` — псевдоним для `COUNT(p.id)`); (б) тот же запрос с повторным выражением агрегата в HAVING. Сравните поведение на H2/Hibernate 6 и зафиксируйте вывод о переносимости в комментарии к тесту.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t04.TASK09Test
// (a) "SELECT c.name, COUNT(p.id) total FROM Category c LEFT JOIN c.products p
//       GROUP BY c.name HAVING total > :min"  -> assertThrows
// (b) "... GROUP BY c.name HAVING COUNT(p.id) > :min" -> результат
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t04.TASK09Test#havingAlias
```
- Критерий приёмки: Тест: вариант (a) бросает `IllegalArgumentException` (обёртка `SemanticException` — «could not interpret path expression») уже при создании запроса — `assertThrows`; вариант (b) корректно отфильтровывает группы; в комментарии зафиксировано: алиасы в HAVING разрешает не всякий SQL-диалект (например, MySQL — да, стандарт/H2 — нет), поэтому портативный код повторяет выражение агрегата.
- Подсказка (не решение): HAVING в SQL-стандарте видит только выражения агрегатов, а не имена-псевдонимы из SELECT.
