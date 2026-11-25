# Задание 02. Persistent существует только внутри транзакции
- Тема: Жизненный цикл Entity
- Difficulty: 2
- Что нужно сделать: Реализуйте в `ProductLifecycleService` метод, который в первой REQUIRES_NEW-транзакции загружает товар по id (`em.find`) и проверяет `em.contains` для этой инстанции, затем — в другой REQUIRES_NEW-транзакции проверяет `em.contains` для **той же** инстанции (локальная переменная). Метод возвращает `boolean[]` `[containsInsideTx, containsAfterCommit]`. Наблюдаем: Persistent — это «находится в persistence context», а PC живёт только внутри своей транзакции; после commit инстанция Detached, но строка в БД осталась.
- Стартовый код:
```java
// TODO: добавьте метод в существующий com.example.jpalab.service.ProductLifecycleService (@Service)
public boolean[] containsInsideAndAfterTransaction(Long id) {
    // TODO: первая REQUIRES_NEW-транзакция — em.find + em.contains;
    // TODO: вторая REQUIRES_NEW-транзакция — em.contains той же инстанции
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t11.TASK02Test#persistentStateExistsOnlyInsideTransaction
```
- Критерий приёмки: Тест: `containsInsideTx == true`, `containsAfterCommit == false`, при этом после commit теста `findById(id)` находит строку (данные не потеряны — потеряна только «управляемость» этой инстанции).
- Подсказка (не решение): одна и та же Java-инстанция может быть managed в одном PC и detached в другом — состояние не у объекта, а у пары (объект, persistence context).
