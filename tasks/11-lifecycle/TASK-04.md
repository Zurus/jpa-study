# Задание 04. merge() на transient-инстанции: INSERT, новая managed-копия
- Тема: Жизненный цикл Entity
- Difficulty: 4
- Что нужно сделать: Реализуйте в `ProductLifecycleService` метод, который в своей REQUIRES_NEW-транзакции создаёт **transient** `Product` (id = null) и вызывает на нём `em.merge(...)`, а не `persist`/`save`. Метод возвращает `Object[]` `[managedId, managedIsNotSameAsInput, inputIdRemainsNull]`: id возвращённой managed-инстанции, сравнение её с исходной (`!=`) и факт, что у исходной инстанции id остался null. Наблюдаем: merge — универсальная операция (transient → INSERT, detached → UPDATE), всегда возвращает managed-инстанцию, а переданный объект «вне игры».
- Стартовый код:
```java
// TODO: добавьте метод в существующий com.example.jpalab.service.ProductLifecycleService (@Service)
public Object[] mergeTransientProduct(String name, BigDecimal price, int stockQuantity) {
    // TODO: new Product (id = null) -> em.merge(...) в REQUIRES_NEW-транзакции;
    // TODO: вернуть [managedId, managedIsNotSameAsInput, inputIdRemainsNull]
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t11.TASK04Test#mergeOfTransientInsertsNewRow
```
- Критерий приёмки: Тест: `managedId` не null; merge вернул инстанцию, отличную от переданной (`isNotSameAs`); у исходной transient-инстанции id остался null; в БД ровно одна строка с переданными значениями, `version = 0`.
- Подсказка (не решение): `merge` копирует состояние переданного объекта в managed-инстанцию (создавая её, если id не найден в БД) — дальше Hibernate работает только с managed-копией.
