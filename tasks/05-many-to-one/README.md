# Тема 5: @ManyToOne (Product → Category)

## Чему учит тема
- «Многие» сторона владеет внешним ключом: `category_id` в таблице `products`; все изменения FK проходят через неё.
- LAZY-прокси, identity первого уровня кэша, dirty checking и merge на стороне ассоциации.
- Пограничные ситуации: transient-категория, удаление «одной» стороны при живых ссылках, откат транзакции.
- Как читать UPDATE-запросы FK в SQL-логе и доказывать поведение тестами (включая `@SpringBootTest` + `TransactionTemplate`).

## Список заданий
| # | Файл | Название | Difficulty |
|---|------|----------|------------|
| 1 | TASK-01.md | Первый ManyToOne: FK null и FK установлен | 1 |
| 2 | TASK-02.md | Смена FK: привязка товара к другой категории | 2 |
| 3 | TASK-03.md | Отвязка: category = null | 3 |
| 4 | TASK-04.md | Transient-категория: исключение без cascade | 4 |
| 5 | TASK-05.md | Identity: одна инстанция категории на два товара | 5 |
| 6 | TASK-06.md | Dirty checking: FK и поле в одном UPDATE | 6 |
| 7 | TASK-07.md | FK-ограничение на уровне БД | 7 |
| 8 | TASK-08.md | Нельзя удалить «одного», пока на него ссылаются | 8 |
| 9 | TASK-09.md | Merge отстёгнутого товара со сменой категории | 9 |
| 10 | TASK-10.md | Откат транзакции: FK возвращается | 10 |

Диапазон difficulty: 1–10.

## Как проверить задание
1. При необходимости добавьте метод в `src/main/java/com/example/jpalab/repository/ProductRepository.java` или `CategoryRepository.java` (только то, что требует задание).
2. Создайте тест-класс `ManyToOneTask01Test` … `ManyToOneTask10Test` в `src/test/java/com/example/jpalab/repository/`. Обычные сценарии — `@DataJpaTest`; для заданий с commit/rollback (TASK-10) — `@SpringBootTest` + `TransactionTemplate` (образец: `lifecycle/EntityLifecycleExampleTest.java`).
3. Запустите: `mvn -Dtest=ManyToOneTask01Test test` (замените имя).
4. Задание выполнено, если тест зелёный и выполнен «Критерий приёмки» (поведение + фрагменты SQL-лога/исключения, где требуется).

Решения в этой папке не раскрываются.
