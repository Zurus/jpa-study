# Задание 09. Ловушка: count-запрос с JOIN FETCH и «мультипликация» строк
- Тема: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
- Difficulty: 9
- Что нужно сделать: Добавьте в `CategoryRepository` метод `List<Category> findAllWithProductsFetched()` — ВСЕ категории с уже загруженными коллекциями товаров ОДНИМ запросом (граф коллекции `products`; пустые категории должны остаться в результате — значит, внутренний LEFT JOIN). Заранее созданный тест (TASK09Test) исследует fetch join коллекции `Category.products`: (а) динамический `count(c)` с `JOIN FETCH c.products` — зафиксировано исключение; (б) `count(c)` с обычным `JOIN c.products` считает joined-строки, а не категории; (в) ваш метод: один запрос, все 4 категории (включая пустую), уникальные инстанции, `getProducts().size()` — без доп. SQL.
- Стартовый код:
```java
// com/example/jpalab/repository/CategoryRepository.java

// TODO: @EntityGraph(attributePaths = "products") + findAll()
List<Category> findAllWithProductsFetched();
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t07.TASK09Test#fetchJoinCountTrap
```
- Критерий приёмки: Тест: данные — категории (3, 2, 1, 0 товаров); (a) count с fetch join бросает `IllegalArgumentException` (обёртка SemanticException: владелец fetch-ассоциации не в select list) — `assertThrows`; (b) `count(c) JOIN c.products` вернул число joined-строк (6), а не число категорий (4); (в) ваш метод: ровно 1 prepared statement, в результате 4 категории (включая пустую "F D"), инстанции уникальны, суммарное число товаров 6 — без единого доп. запроса к коллекциям; в комментарии выводы: count для страницы нельзя строить «на коленке» поверх fetch join, а plain-join в count «мультиплицирует» строки.
- Подсказка (не решение): JOIN в count-выражении считает СТРОКИ соединения, а count + fetch join Hibernate запрещает; чтобы пустые категории не выпадали, граф строит LEFT JOIN.
