# Тема 8: Пагинация (Page / Slice / Pageable / Sort)

## Чему учит тема
- `Page` (полные метаданные, count-запрос) против `Slice` (только hasNext, «лишняя» строка, без count).
- `Pageable`/`PageRequest`/`Sort`: сортировка, направление, тай-брейки; пагинация вместе с фильтрацией и сортировкой.
- Цена пагинации: 2 SQL у `Page` (count + данные) против 1 у `Slice`; N+1 на странице (LAZY-ассоциации).
- Ловушки: нестабильный порядок без уникального ключа сортировки, pages «за концом» и size = 0, count по join-запросам считает joined-строки.

## Список заданий
| # | Файл | Название | Difficulty |
|---|------|----------|------------|
| 1 | TASK-01.md | Первый Page | 1 |
| 2 | TASK-02.md | Sort внутри Pageable | 2 |
| 3 | TASK-03.md | Метаданные Page | 3 |
| 4 | TASK-04.md | Page + фильтрация | 4 |
| 5 | TASK-05.md | Фильтр + сортировка + пагинация вместе | 5 |
| 6 | TASK-06.md | Page vs Slice: цена в SQL | 6 |
| 7 | TASK-07.md | Граничные PageRequest: «за концом» и size = 0 | 7 |
| 8 | TASK-08.md | Нестабильный порядок: тай-брейк по уникальному ключу | 8 |
| 9 | TASK-09.md | Пагинация + LAZY: N+1 на странице и лечение @EntityGraph | 9 |
| 10 | TASK-10.md | Slice + фильтр + Sort: механика «лишней» строки | 10 |

Диапазон difficulty: 1–10.

## Как проверить задание
1. Новые методы с `Pageable`/`Slice`/`@EntityGraph` добавляйте в `src/main/java/com/example/jpalab/repository/ProductRepository.java` только в объёме задания (образцы методов уже есть в репозитории).
2. Создайте тест-класс `PaginationTask01Test` … `PaginationTask10Test` в `src/test/java/com/example/jpalab/repository/` (`@DataJpaTest`, AssertJ).
3. Для подсчёта SQL используйте Statistics (образец: `PaginationExampleTest.java`); для честного замера создавайте данные, затем `em.flush(); em.clear();`.
4. Запустите: `mvn -Dtest=PaginationTask01Test test` (замените имя).

Решения в этой папке не раскрываются.
