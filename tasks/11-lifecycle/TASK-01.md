# Задание 01. Transient → Persistent: persist и commit
- Тема: Жизненный цикл Entity
- Difficulty: 1
- Что нужно сделать: В сервисе `ProductLifecycleService` (контракт — в `tasks/11-lifecycle/README.md`) реализуйте метод, который в своей REQUIRES_NEW-транзакции создаёт новый `Product` (имя, цена, остаток — из аргументов), вызывает на нём `em.persist` и закоммичивает транзакцию; метод возвращает присвоенный id. Наблюдаем: у transient-сущности id до persist не было, после commit — ровно одна строка в БД.
- Стартовый код:
```java
// TODO: добавьте метод в существующий com.example.jpalab.service.ProductLifecycleService (@Service)
// TODO: метод в новой транзакции (REQUIRES_NEW):
public Long persistProduct(String name, BigDecimal price, int stockQuantity) {
    // TODO
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t11.TASK01Test#persistCreatesRowAfterCommit
```
- Критерий приёмки: Тест: id возвращён (не null — у IDENTITY id проставляется при самом persist), после commit сервиса в БД ровно одна строка с переданными ценой/остатком, `version = 0`.
- Подсказка (не решение): `em.persist` требует открытую транзакцию — без неё Hibernate бросит `TransactionRequiredException`.
