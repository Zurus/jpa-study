# Задание 09. Ловушка: алиас агрегата в HAVING
- Тема: GROUP BY / HAVING
- Difficulty: 9
- Что нужно сделать: Реализуйте в `ProductRepository` метод `List<Object[]> categoryNamesWithMoreProducts(@Param("minCount") long minCount)` — JPQL-запрос: группировка по категориям (LEFT JOIN по товарам), возвращает пары (имя категории, число товаров) для групп, где число товаров строго больше `:minCount`. Заранее созданный тест (TASK09Test) показывает: (а) динамический JPQL с АЛИАСОМ агрегата в HAVING (`HAVING total > :min`) падает уже при создании запроса; (б) ваш метод (с повторным выражением агрегата в HAVING) работает и отфильтровывает пустые группы.
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Query (JPQL) — SELECT c.name, COUNT(p.id) ... GROUP BY c.name
//       HAVING COUNT(p.id) > :minCount (агрегат повторяем, алиас в HAVING не портируем)
List<Object[]> categoryNamesWithMoreProducts(@Param("minCount") long minCount);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t04.TASK09Test#havingAlias
```
- Критерий приёмки: Тест: данные — категории "Many" (2 товара) и "Empty" (0 товаров); вариант (а) — `assertThrows(IllegalArgumentException)` уже на `createQuery`; ваш метод с `minCount = 0` возвращает ровно одну пару ("Many", 2) — пустая группа отфильтрована; в комментарии зафиксировано: алиасы в HAVING разрешает не всякий SQL-диалект, поэтому портативный код повторяет выражение агрегата.
- Подсказка (не решение): HAVING в SQL-стандарте видит только выражения агрегатов, а не имена-псевдонимы из SELECT.
