# Задание 09. Ловушка: count-запрос с JOIN FETCH и «мультипликация» строк
- Тема: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
- Difficulty: 9
- Что нужно сделать: Тест создан заранее (TASK09Test) с динамическими запросами (`EntityManager`), исследующий fetch join коллекции `Category.products`: (а) `count(c)` с `JOIN FETCH c.products` — зафиксировано исключение; (б) `count(c)` с обычным `JOIN c.products` — сравнение с `count(c)` без join на данных, где у категорий разное число товаров; (в) content-запрос с fetch join и лимитом — проверка, что сущности в результате не дублируются. Методы в репозиторий добавлять не нужно (битый fetch join в `@Query` убил бы запуск приложения).
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t07.TASK09Test
// данные: категории "F A" (3 товара), "F B" (2), "F C" (1), "F D" (0)
// (a) assertThrows(IllegalArgumentException, () -> em.createQuery(
//        "SELECT count(c) FROM Category c JOIN FETCH c.products", Long.class).getSingleResult())
// (b) count c JOIN c.products -> 6 (joined-строки),  count c без join -> 4 (категории)
// (c) SELECT c ... JOIN FETCH c.products, setMaxResults(3) -> инстанции уникальны
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t07.TASK09Test#fetchJoinCountTrap
```
- Критерий приёмки: Тест: (a) count с fetch join бросает `IllegalArgumentException` (обёртка SemanticException: владелец fetch-ассоциации не в select list) — `assertThrows`; (b) `count(c) JOIN c.products` вернул число joined-строк (6), а не число категорий (4) — оба значения зафиксированы ассертами; (c) лимитированный fetch-join-запрос не содержит дубликатов инстанции (identity-проверка); в комментарии выводы: count для страницы нельзя строить «на коленке» поверх fetch join, а plain-join в count «мультиплицирует» строки.
- Подсказка (не решение): JOIN в count-выражении считает СТРОКИ соединения, а count + fetch join Hibernate запрещает.
