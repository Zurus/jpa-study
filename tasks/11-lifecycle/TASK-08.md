# Задание 08. em.clear(): новый identity map внутри транзакции
- Тема: Жизненный цикл Entity
- Difficulty: 8
- Что нужно сделать: Реализуйте в `ProductLifecycleService` метод, который в **одной** REQUIRES_NEW-транзакции: загружает товар по id (`em.find`), вызывает `em.clear()`, снова загружает товар по тому же id и возвращает `Object[]` `[differentInstances, sameId]` — сравнение двух инстанций (`!=`) и равенство их id. Наблюдаем: `em.clear()` сбрасывает identity map (и всех managed-сущностей) внутри той же транзакции — повторный `find` делает новый SELECT и возвращает новую инстанцию.
- Стартовый код:
```java
// TODO: добавьте метод в существующий com.example.jpalab.service.ProductLifecycleService (@Service)
public Object[] identityMapAfterClear(Long id) {
    // TODO: одна REQUIRES_NEW-транзакция:
    // TODO: a = em.find(...); em.clear(); b = em.find(...);
    // TODO: вернуть [a != b, a.getId().equals(b.getId())]
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t11.TASK08Test#clearResetsIdentityMap
```
- Критерий приёмки: Тест: `differentInstances == true`, `sameId == true`. В SQL-логе две одинаковые выборки по id (после `clear` кэш пуст — второй запрос пошёл в БД).
- Подсказка (не решение): `em.clear()` — самый простой способ «выбросить» stale-данные из PC, не откатывая транзакцию (то же делает флаг `clearAutomatically` у `@Modifying` — тема 09).
