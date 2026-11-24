# Тема 2: JPQL @Query

## Чему учит тема
- JPQL работает с моделью СУЩНОСТЕЙ (классы и свойства), а не с таблицами — запрос переносим между СУБД.
- Именованные параметры (`:name` + `@Param`), функции (UPPER/LOWER/CONCAT), LIKE, явные JOIN, подзапросы.
- Проекция: `Object[]`, constructor expression (`SELECT new ...`) — выбор только нужных полей.
- Ловушки: колонки БД в JPQL не работают, implicit-join скрывает строки с NULL, результат запроса живёт в persistence context (L1-кэш).

## Список заданий
| # | Файл | Название | Difficulty |
|---|------|----------|------------|
| 1 | TASK-01.md | Первый JPQL-запрос с именованным параметром | 1 |
| 2 | TASK-02.md | Детерминированная сортировка с тай-брейком | 2 |
| 3 | TASK-03.md | Явный JOIN к ассоциации | 3 |
| 4 | TASK-04.md | Регистронезависимый поиск: UPPER/CONCAT/LIKE | 4 |
| 5 | TASK-05.md | Проекция в Object[] | 5 |
| 6 | TASK-06.md | Constructor expression (DTO-проекция) | 6 |
| 7 | TASK-07.md | JPQL + пагинация Pageable | 7 |
| 8 | TASK-08.md | Ловушка: свойство сущности vs колонка БД | 8 |
| 9 | TASK-09.md | Подзапрос IN | 9 |
| 10 | TASK-10.md | L1-кэш: запрос и изменённая managed-сущность | 10 |

Диапазон difficulty: 1–10.

## Как проверить задание
1. Добавьте аннотируемый метод `@Query` в `src/main/java/com/example/jpalab/repository/ProductRepository.java` согласно заданию.
2. Создайте тест-класс `JpqlTask01Test` … `JpqlTask10Test` в `src/test/java/com/example/jpalab/repository/` (`@DataJpaTest`, AssertJ, свои данные в каждом тесте). Для заданий с динамическими запросами используйте `@PersistenceContext EntityManager`.
3. Запустите: `mvn -Dtest=JpqlTask01Test test` (замените имя).
4. Задание выполнено, если тест зелёный и выполнены пункты «Критерий приёмки» (поведение + фрагменты SQL-лога, где требуется).

Решения в этой папке не раскрываются.
