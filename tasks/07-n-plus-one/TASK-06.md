# Задание 06. Batch fetching: default_batch_fetch_size
- Тема: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
- Difficulty: 6
- Что нужно сделать: Тест создан заранее (TASK06Test) с `@DataJpaTest(properties = "spring.jpa.properties.hibernate.default_batch_fetch_size=2")`: 6 товаров в 3 категориях (по 3, 2, 1). Товары загружаются, в цикле инициализируются категории — показано, что запросы на категории собрались в `IN`-батчи (2 SELECT вместо 3 отдельных). Методы в репозиторий добавлять не нужно.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t07.TASK06Test
// Ключевые шаги:
//   6 товаров / 3 категории (3, 2, 1); em.flush(); em.clear();
//   statistics.clear();
//   List<Product> ps = productRepository.findAll();   // 1
//   for (Product p : ps) p.getCategory().getName();   // +2 (батчи по 2 id)
//   итого 3; в SQL-логе: where category_id in (?, ?) и in (?)
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t07.TASK06Test#defaultBatchFetchSizeCollapsesProxyLoads
```
- Критерий приёмки: Тест: `findAll` — 1 statement; инициализация 3 УНИКАЛЬНЫХ категорий через цикл (повторы категории не плодят запросы) — ровно 2 prepared statement (батчи по 2 id); в комментарии зафиксировано: batch size задан свойством контекста, а без него было бы 3 отдельных SELECT.
- Подсказка (не решение): `default_batch_fetch_size` собирает запросы прокси в `IN`-списки — N+1 сжимается в ceil(N/batchSize).
