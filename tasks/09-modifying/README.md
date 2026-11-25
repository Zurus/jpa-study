# Тема 9: @Modifying (bulk UPDATE / DELETE)

## Чему учит тема
- `@Modifying` + `@Query`: bulk UPDATE/DELETE в JPQL; возвращаемое значение — число затронутых строк.
- Bulk-запросы меняют БД напрямую, минуя persistence context: «устаревшие» managed-сущности, `em.clear()`, флаг `clearAutomatically`.
- Флаг `flushAutomatically`: pending-изменения PC выгружаются в БД ПЕРЕД выполнением bulk-запроса — запрос видит несуществующие ещё в БД изменения.
- Bulk SQL против JPA-пути: cascade/orphanRemoval, @Version, lifecycle-колбэки НЕ выполняются.
- Ловушки: «перезапись» bulk UPDATE изменением устаревшей сущности (@DynamicUpdate не стоит → UPDATE содержит все столбцы), FK-нарушение bulk DELETE, @Modifying вне транзакции.

## Список заданий
| # | Файл | Название | Difficulty |
|---|------|----------|------------|
| 1 | TASK-01.md | Первый bulk UPDATE | 1 |
| 2 | TASK-02.md | Bulk DELETE по условию | 2 |
| 3 | TASK-03.md | Конвейер: bulk UPDATE → bulk DELETE | 3 |
| 4 | TASK-04.md | Несколько SET-полей и выражение в правой части | 4 |
| 5 | TASK-05.md | Коллекционный параметр `IN :names` | 5 |
| 6 | TASK-06.md | clearAutomatically: PC очищается после запроса | 6 |
| 7 | TASK-07.md | flushAutomatically: запрос видит незаflushенные изменения | 7 |
| 8 | TASK-08.md | Ловушка: «устаревшая» сущность переписывает bulk UPDATE | 8 |
| 9 | TASK-09.md | Ловушка: bulk DELETE обходит JPA cascade | 9 |
| 10 | TASK-10.md | Оба флага в реальной транзакции | 10 |

Диапазон difficulty: 1–10.

## Как проверить задание
1. Новые методы добавляйте в `src/main/java/com/example/jpalab/repository/ProductRepository.java` (или `CategoryRepository.java`) — строго в объёме задания (образцы `@Modifying`-методов уже есть в `ProductRepository`, раздел «Тема 9»).
2. Тесты уже созданы (пакет `com.example.jpalab.tasks.t09`, по классу на задание): запустите команду из блока «Тест для проверки» в файле задания.
3. Задание выполнено, когда тест зелёный и выполнены пункты «Критерии приёмки» (поведение + фрагменты SQL-лога, где это требуется).

Решения в этой папке не раскрываются.
