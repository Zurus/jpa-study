# Тема 4: GROUP BY / HAVING

## Чему учит тема
- Агрегаты (COUNT, SUM, AVG, MIN, MAX) и группировка: что может стоять в SELECT/GROUP BY, а что — только под агрегатом.
- `HAVING` фильтрует группы (в отличие от `WHERE`, фильтрующего строки); LEFT JOIN сохраняет «пустые» группы.
- NULL-семантика агрегатов: `SUM` пустой группы — NULL, `COUNT(*)` vs `COUNT(col)`.
- Ловушки: алиас агрегата в `HAVING` не переносим (H2/Hibernate его отклоняют), `Page` по сгруппированному запросу считает ГРУППЫ, а не строки.

## Список заданий
| # | Файл | Название | Difficulty |
|---|------|----------|------------|
| 1 | TASK-01.md | Первый GROUP BY | 1 |
| 2 | TASK-02.md | Сортировка по агрегату | 2 |
| 3 | TASK-03.md | GROUP BY через ассоциацию | 3 |
| 4 | TASK-04.md | Несколько агрегатов в одной строке | 4 |
| 5 | TASK-05.md | HAVING: фильтр по группам | 5 |
| 6 | TASK-06.md | LEFT JOIN: пустые группы | 6 |
| 7 | TASK-07.md | AVG + HAVING: категории по средней цене | 7 |
| 8 | TASK-08.md | NULL и COUNT: пустая группа | 8 |
| 9 | TASK-09.md | Ловушка: алиас агрегата в HAVING | 9 |
| 10 | TASK-10.md | Page по сгруппированному запросу | 10 |

Диапазон difficulty: 1–10.

## Как проверить задание
1. Добавьте метод `@Query` (JPQL) в `src/main/java/com/example/jpalab/repository/ProductRepository.java` или `CategoryRepository.java` согласно заданию.
2. Создайте тест-класс `GroupByTask01Test` … `GroupByTask10Test` в `src/test/java/com/example/jpalab/repository/` (`@DataJpaTest`, AssertJ, свои данные в каждом тесте). Для заданий с динамическими запросами используйте `@PersistenceContext EntityManager`.
3. Запустите: `mvn -Dtest=GroupByTask01Test test` (замените имя).
4. Задание выполнено, если тест зелёный и выполнены пункты «Критерий приёмки» (поведение + фрагменты SQL-лога, где требуется).

Решения в этой папке не раскрываются.
