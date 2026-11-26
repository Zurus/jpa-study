# Задание 08. Ловушка: свойство сущности vs колонка БД
- Тема: JPQL @Query
- Difficulty: 8
- Что нужно сделать: Тест создан заранее (TASK08Test): он строит динамические запросы через `EntityManager`: (а) JPQL с именем КОЛОНКИ `p.stock_quantity` — ошибка; (б) тот же запрос с именем СВОЙСТВА `p.stockQuantity` — работает. Дополнительно реализуйте в `ProductRepository` метод `List<Product> findStockAtLeast(@Param("minStock") int minStock)` — JPQL-запрос, возвращающий товары с остатком строго больше `:minStock` (используя имя СВОЙСТВА); тест требует, чтобы он дал тот же результат, что и вариант (б).
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Query (JPQL) — товары со stockQuantity > :minStock
List<Product> findStockAtLeast(@Param("minStock") int minStock);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t02.TASK08Test#columnVsProperty
```
- Критерий приёмки: Тест: вариант (а) бросает `IllegalArgumentException` (обёртка `SemanticException`: путь не распознан); вариант (б) и ваш метод `findStockAtLeast(10)` на данных (stock 5 и 15) возвращают ровно товар `High`; в комментарии к тесту зафиксировано: JPQL — язык модели сущностей, имена колонок существуют только в DDL и native-запросах, поэтому «колонка» в JPQL-методе репозитория убьёт запуск приложения.
- Подсказка (не решение): в JPQL пишите `camelCase`-свойства; `snake_case`-колонки Hibernate подставит сам.
