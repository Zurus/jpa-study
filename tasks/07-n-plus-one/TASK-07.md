# Задание 07. Сравнение: LAZY vs JOIN FETCH vs @EntityGraph
- Тема: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
- Difficulty: 7
- Что нужно сделать: Тест создан заранее (TASK07Test): ОДИН тест с тремя фазами на одних и тех же 4 товарах (4 категории). Фаза 1 — `findAll` + доступ к категориям (чистый LAZY); фаза 2 — `JOIN FETCH`-метод `findAllWithCategoryJoined` (добавьте в `ProductRepository` по заданию 03, если ещё не добавили); фаза 3 — `@EntityGraph`-метод `findAllByPriceGreaterThan` (добавьте по заданию 04). В каждой фазе: `em.clear()`, сброс статистики, замер. «Таблица» результатов собрана в ассертах.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t07.TASK07Test
// данные: 4 товара / 4 категории (один раз, до фаз)
// фаза 1: em.clear(); stats.clear(); findAll + цикл        -> 5 (1 + 4)
// фаза 2: em.clear(); stats.clear(); findAllWithCategoryJoined() + цикл -> 1
// фаза 3: em.clear(); stats.clear(); findAllByPriceGreaterThan(0) + цикл -> 1
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t07.TASK07Test#compareLazyFetchJoinAndEntityGraph
```
- Критерий приёмки: Тест: все три фазы в одном тесте на идентичных данных; фаза 1 — 1 + N (5) prepared statement, фазы 2 и 3 — по 1; после фаз 2 и 3 все категории инициализированы; в комментарии к тесту «таблица»: стратегия / число SQL / когда уместна (fetch join — когда ассоциация всегда нужна; batch — когда нужна частично; LAZY — когда почти не нужна).
- Подсказка (не решение): сравнивайте при равных условиях: одни данные, `em.clear()` и сброс статистики перед каждой фазой.
