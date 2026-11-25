# Задание 05. Dirty checking: UPDATE без явного save
- Тема: Жизненный цикл Entity
- Difficulty: 5
- Что нужно сделать: Реализуйте в `ProductLifecycleService` метод, который в своей REQUIRES_NEW-транзакции загружает товар по id (`em.find`), меняет ему цену (`setPrice`) и **заканчивает транзакцию, не вызывая save/persist/flush явно**. Наблюдаем: при flush в конце транзакции Hibernate сравнивает состояние managed-инстанции с состоянием на момент загрузки и сам генерирует `UPDATE`, увеличивая `@Version`.
- Стартовый код:
```java
// TODO: добавьте метод в существующий com.example.jpalab.service.ProductLifecycleService (@Service)
public void updatePriceByDirtyChecking(Long id, BigDecimal newPrice) {
    // TODO: em.find -> setPrice -> commit (БЕЗ явного save/persist/flush)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t11.TASK05Test#dirtyCheckingUpdatesWithoutSave
```
- Критерий приёмки: Тест: цена в БД стала новой; `version` вырос с 0 до 1 (UPDATE был сгенерирован dirty checking'ом, а не явным сохранением); прочие поля строки не изменились.
- Подсказка (не решение): dirty checking срабатывает только для managed-инстанций (Persistent) — изменение detached-объекта БД не двигает.
