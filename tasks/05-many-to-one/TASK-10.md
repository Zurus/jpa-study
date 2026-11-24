# Задание 10. Откат транзакции: FK возвращается
- Тема: @ManyToOne (Product → Category)
- Difficulty: 10
- Что нужно сделать: Тест в РЕАЛЬНЫХ транзакциях создан заранее (TASK10Test, `@SpringBootTest` + `TransactionTemplate`): в транзакции товар переводится из категории A в B и транзакция завершается ОТКАТОМ. Доказывается, что в новой транзакции товар снова в A: и по значению FK в БД, и по состоянию при свежей загрузке. Тест самодостаточен (свои setup/commit/очистка).
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t05.TASK10Test
// @SpringBootTest + TransactionTemplate (см. @TestConfiguration TestBeans)
// setup (отдельная tx, commit): A, B, товар в A;
// tx: setCategory(B) + flush -> принудительный rollback;
// новая tx: SELECT category_id -> id(A); новая загрузка -> категория "A"
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK10Test#rollbackRestoresFk
```
- Критерий приёмки: Тест: после отката `category_id` в БД — id категории A (UPDATE не зафиксирован); свежая загрузка товара показывает категорию A; в SQL-логе откатившейся транзакции виден и `update products ... category_id`, и `rollback` (обновление выполнялось, но не пошло в БД); в комментарии зафиксировано: dirty checking и UPDATE — часть транзакции, откат отменяет и их.
- Подсказка (не решение): пока транзакция не закоммичена, «обновлённый» FK живёт только в БД-транзакции — rollback его стирает.
