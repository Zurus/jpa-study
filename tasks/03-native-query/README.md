# Тема 3: Native queries

## Чему учит тема
- Native-запрос — «сырой» SQL против реальных таблиц и колонок (snake_case DDL-имена), `nativeQuery = true`.
- Результат маппится автоматически: сущность по `*`, `Object[]` по списку колонок, скаляр по одной колонке.
- Пагинация работает через JDBC (`setFirstResult`/`setMaxResults`), а не через `LIMIT` в тексте запроса.
- Ловушки: нет constructor expression, результат попадает в persistence context (managed), LAZY-ассоциации остаются прокси, `NULL = NULL` — не true.

## Список заданий
| # | Файл | Название | Difficulty |
|---|------|----------|------------|
| 1 | TASK-01.md | Первый native-запрос | 1 |
| 2 | TASK-02.md | Явный список колонок и ORDER BY (snake_case) | 2 |
| 3 | TASK-03.md | Проекция в Object[] | 3 |
| 4 | TASK-04.md | JOIN в native SQL | 4 |
| 5 | TASK-05.md | Одна колонка — скалярный результат | 5 |
| 6 | TASK-06.md | Native + пагинация (Pageable) | 6 |
| 7 | TASK-07.md | Ловушка: constructor expression в native не работает | 7 |
| 8 | TASK-08.md | NULL в native SQL | 8 |
| 9 | TASK-09.md | Результат native-запроса — managed-сущность | 9 |
| 10 | TASK-10.md | Native-запрос и LAZY-ассоциации: fetch невозможен | 10 |

Диапазон difficulty: 1–10.

## Как проверить задание
1. Добавьте аннотируемый метод `@Query(..., nativeQuery = true)` в `src/main/java/com/example/jpalab/repository/ProductRepository.java` согласно заданию.
2. Создайте тест-класс `NativeTask01Test` … `NativeTask10Test` в `src/test/java/com/example/jpalab/repository/` (`@DataJpaTest`, AssertJ, свои данные в каждом тесте). Для заданий с динамическими запросами используйте `@PersistenceContext EntityManager`.
3. Запустите: `mvn -Dtest=NativeTask01Test test` (замените имя).
4. Задание выполнено, если тест зелёный и выполнены пункты «Критерий приёмки» (поведение + фрагменты SQL-лога, где требуется).

Решения в этой папке не раскрываются.
