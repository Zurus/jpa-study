# Задание 10. Полный прогон жизненного цикла
- Тема: Жизненный цикл Entity
- Difficulty: 10
- Что нужно сделать: Реализуйте в `ProductLifecycleService` метод-финал темы: один вызов — весь жизненный цикл, каждый шаг в своей REQUIRES_NEW-транзакции: (1) transient → persistent (`em.persist`); (2) dirty checking — `find` + смена цены на `price + 10.00` без явного save; (3) detached-изменение — `find` + `em.detach` + смена цены на `price + 20.00` **без merge** (изменение должно потеряться); (4) removed — `find` + `em.remove` + commit; (5) контрольные чтения. Метод возвращает `Map<String, Object>` с ключами: `"id"` (Long), `"versionAfterDirty"` (Long — version после шага 2), `"priceAfterDetachedMutation"` (BigDecimal — цена, реально лежащая в БД после шага 3), `"existsAfterRemove"` (Boolean — есть ли строка в БД после шага 4), `"findableAfterRemove"` (Boolean — находит ли `em.find` сущность после commit удаления).
- Стартовый код:
```java
// TODO: добавьте метод в существующий com.example.jpalab.service.ProductLifecycleService (@Service)
public Map<String, Object> fullLifecycleWalk(String name, BigDecimal price, int stockQuantity) {
    // TODO: шаги 1–5, каждый в своей REQUIRES_NEW-транзакции;
    // TODO: вернуть Map с ключами id, versionAfterDirty,
    //      priceAfterDetachedMutation, existsAfterRemove, findableAfterRemove
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t11.TASK10Test#fullLifecycleWalk
```
- Критерий приёмки: Тест: `id` не null; `versionAfterDirty == 1` (dirty checking увеличил version с 0); `priceAfterDetachedMutation == price + 10.00` (dirty-изменение из шага 2 в БД, а detached-изменение из шага 3 без merge — потеряно); `existsAfterRemove == false` и `findableAfterRemove == false`; по имени в БД строк нет.
- Подсказка (не решение): порядок шагов важен — detached-шаг (3) идёт ПОСЛЕ committed dirty-обновления (2), поэтому «правильная» цена в БД — `price + 10.00`, а не `price + 20.00`.
