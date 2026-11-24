# Тема 7: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)

## Чему учит тема
- Как рождается N+1: 1 запрос на сущности + N запросов на LAZY-ассоциации; измерение числа SQL через Hibernate Statistics.
- Три противоядия и когда они работают: `JOIN FETCH` (одна SELECT с join), `@EntityGraph` (fetch join «из коробки»), batch fetching (`IN`-батчи для прокси/коллекций).
- Граничные случаи: fetch join + фильтрация/сортировка, fetch join + пагинация, «мультипликация» строк при fetch join коллекции.
- Все задания требуют точного счёта prepared statements — «на глаз» N+1 не доказывают.

## Список заданий
| # | Файл | Название | Difficulty |
|---|------|----------|------------|
| 1 | TASK-01.md | Измерить N+1 на @ManyToOne | 1 |
| 2 | TASK-02.md | Измерить N+1 на @OneToMany | 2 |
| 3 | TASK-03.md | Лечение JOIN FETCH (to-one) | 3 |
| 4 | TASK-04.md | Лечение @EntityGraph | 4 |
| 5 | TASK-05.md | JOIN FETCH с WHERE и ORDER BY | 5 |
| 6 | TASK-06.md | Batch fetching: default_batch_fetch_size | 6 |
| 7 | TASK-07.md | Сравнение: LAZY vs JOIN FETCH vs @EntityGraph | 7 |
| 8 | TASK-08.md | N+1 под пагинацией | 8 |
| 9 | TASK-09.md | Ловушка: count-запрос с JOIN FETCH и «мультипликация» строк | 9 |
| 10 | TASK-10.md | N+1 в сервисной логике (реальная транзакция) | 10 |

Диапазон difficulty: 1–10.

## Как проверить задание
1. Методы с `@Query`/`@EntityGraph` добавляйте в `src/main/java/com/example/jpalab/repository/ProductRepository.java` (или `CategoryRepository.java`) только в объёме задания.
2. Создайте тест-класс `NPlusOneTask01Test` … `NPlusOneTask10Test` в `src/test/java/com/example/jpalab/repository/` (`@DataJpaTest`, AssertJ).
3. Для замера SQL используйте Statistics: `em.unwrap(Session.class).getSessionFactory().getStatistics().getPrepareStatementCount()` (и `clear()` перед замером) — образец: `NPlusOneExampleTest.java`.
4. ВАЖНО: данные создавайте, затем `em.flush(); em.clear();` — иначе `findAll` вернёт те же инстанции из первого уровня кэша (с уже инициализированными ассоциациями), и N+1 не проявится.
5. Запустите: `mvn -Dtest=NPlusOneTask01Test test` (замените имя).

Решения в этой папке не раскрываются.
