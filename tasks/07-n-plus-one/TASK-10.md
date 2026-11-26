# Задание 10. N+1 в сервисной логике (реальная транзакция)
- Тема: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
- Difficulty: 10
- Что нужно сделать: Два артефакта. (1) В `ProductService` — метод `List<String> listProductsWithCategoryNames()` (см. задание 01: «наивный» сценарий, строки `"<name> [<categoryName>]"`). (2) В `ProductRepository` — метод `List<Product> findAllWithCategoryGraph()` — тот же список ВСЕХ товаров, но граф загружается одним запросом через `@EntityGraph` по ассоциации `category`. Заранее созданный тест (TASK10Test) в реальной транзакции (`@SpringBootTest` + `TransactionTemplate`) сравнивает фазы: «наивно» (1 + N запросов) против `@EntityGraph` (1 запрос), строки в обеих фазах одинаковые.
- Стартовый код:
```java
// com/example/jpalab/service/ProductService.java

// TODO: как в задании 01 — наивный сценарий
List<String> listProductsWithCategoryNames();
```
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @EntityGraph(attributePaths = "category") + findAll()
List<Product> findAllWithCategoryGraph();
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t07.TASK10Test#serviceScenarioNaiveVsFetch
```
- Критерий приёмки: Тест: 5 товаров / 5 категорий в реальной транзакции; наивная фаза (ваш сервисный метод) — 6 prepared statement (1 + 5), фаза `findAllWithCategoryGraph()` — 1; собранные строки в обеих фазах совпадают; в комментарии зафиксировано: в сервисном коде выбор fetch-стратегии — вопрос производительности на каждую страницу/список, а не «оптимизация для галочки».
- Подсказка (не решение): считайте запросы в ТЕХ ЖЕ транзакциях, что и в продакшен-сценарии: `@DataJpaTest`-транзакция вокруг всего теста скрывает часть деталей (flush, commit).
