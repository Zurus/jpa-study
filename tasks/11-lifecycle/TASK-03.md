# Задание 03. save() на новом экземпляре: маршрутизация в persist
- Тема: Жизненный цикл Entity
- Difficulty: 3
- Что нужно сделать: Реализуйте в `ProductLifecycleService` метод, который в своей REQUIRES_NEW-транзакции создаёт новый `Product` и сохраняет его через `productRepository.save(...)` (НЕ через `em.persist` — в этом задание о маршрутизации `save`), и возвращает `Object[]` `[id, inputIdSet]`: id сохранённой сущности и факт, что id был проставлен на **исходный** (вошедший в `save`) экземпляр.
- Стартовый код:
```java
// TODO: добавьте метод в существующий com.example.jpalab.service.ProductLifecycleService (@Service)
public Object[] saveNewProduct(String name, BigDecimal price, int stockQuantity) {
    // TODO: new Product -> productRepository.save(...) в REQUIRES_NEW-транзакции;
    // TODO: вернуть [id, inputIdSet]
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t11.TASK03Test#saveRoutesNewInstanceToPersist
```
- Критерий приёмки: Тест: id не null; `inputIdSet == true` (у IDENTITY INSERT идёт сразу при persist, который вызвал `save`, — id виден на исходном объекте без доп. flush); в БД ровно одна строка с переданными значениями, `version = 0`.
- Подсказка (не решение): внутри `SimpleJpaRepository.save` решение «persist или merge» принимается по `isNew(entity)` — по id (для сущностей без `@Version` в `isNew` учитывается и `@Version`).
