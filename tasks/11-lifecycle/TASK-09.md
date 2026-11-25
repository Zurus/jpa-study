# Задание 09. Rollback: id присвоен, строки нет
- Тема: Жизненный цикл Entity
- Difficulty: 9
- Что нужно сделать: Реализуйте в `ProductLifecycleService` метод, который в своей REQUIRES_NEW-транзакции создаёт `Product`, вызывает `em.persist`, а затем **откатывает** транзакцию (`TransactionStatus.setRollbackOnly()` до commit) и возвращает `Long[]` `[id]` — id, который был присвоен при persist. Наблюдаем ловушку IDENTITY-стратегии: INSERT выполняется сразу при persist, id проставляется в инстанцию, но rollback отменяет запись в БД — id «существует» в памяти, а строки в таблице нет.
- Стартовый код:
```java
// TODO: добавьте метод в существующий com.example.jpalab.service.ProductLifecycleService (@Service)
public Long[] rollbackInsertProduct(String name, BigDecimal price, int stockQuantity) {
    // TODO: REQUIRES_NEW-транзакция: em.persist -> status.setRollbackOnly();
    // TODO: вернуть [id]
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t11.TASK09Test#rollbackDiscardsIdentityInsert
```
- Критерий приёмки: Тест: возвращённый id не null (у IDENTITY он присваивается до commit), но `count(*)` по имени в БД = 0 — rollback отменил INSERT. Не-null id ≠ «сохранено в БД»: факт сохранения проверяется только состоянием БД/транзакции.
- Подсказка (не решение): `setRollbackOnly()` откатывает транзакцию при её завершении без броска исключения наружу — идеально для контролируемого отката.
