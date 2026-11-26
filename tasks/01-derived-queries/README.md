# Тема 1: Derived queries (вывод запроса из имени метода)

## Чему учит тема
- Spring Data JPA собирает JPQL из имени метода: предикаты (GreaterThan, Between, Containing), коннекторы (And/Or), Top/First, OrderBy.
- Работает свойство-путь через ассоциацию (`CategoryName`) без явного JOIN.
- Показывает границы конструкции: derived query возвращает сущности целиком (N+1 на LAZY-связях), не умеет fetch join и projection — для этого нужны JPQL/`@EntityGraph`.
- Демонстрирует ловушки: параметр null, порядок параметров, включаемость границ.

## Список заданий
| # | Файл | Название | Difficulty |
|---|------|----------|------------|
| 1 | TASK-01.md | Простой предикат GreaterThan | 1 |
| 2 | TASK-02.md | Поиск без учёта регистра (IgnoreCase) | 2 |
| 3 | TASK-03.md | Top + OrderBy без предиката | 3 |
| 4 | TASK-04.md | Комбинация условий: Containing + Between | 4 |
| 5 | TASK-05.md | Свойство-путь через ассоциацию + First | 5 |
| 6 | TASK-06.md | Коннектор Or и параметр Sort | 6 |
| 7 | TASK-07.md | Пагинация Pageable в derived query | 7 |
| 8 | TASK-08.md | NULL в derived queries | 8 |
| 9 | TASK-09.md | N+1: derived query + LAZY-ассоциация | 9 |
| 10 | TASK-10.md | Порядок параметров: два параметра одного типа | 10 |

Диапазон difficulty: 1–10.

## Как проверить задание
1. Выполните «Что нужно сделать» из TASK-XX.md: добавьте требуемый метод в `src/main/java/com/example/jpalab/repository/ProductRepository.java` (для заданий про категории — `CategoryRepository.java`).
2. Тесты заданий созданы заранее (пакет `com.example.jpalab.tasks.t01`, по классу на задание: `TASK01Test` … `TASK10Test`) — свои тесты писать не нужно.
3. Запустите команду из блока «Тест для проверки» в TASK-XX.md.
4. Задание выполнено, когда тест зелёный и выполнен «Критерий приёмки» из TASK-XX.md (поведение + фрагменты SQL-лога, где требуется).

Решения в этой папке не раскрываются.
