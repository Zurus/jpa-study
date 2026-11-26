# Задание 04. Auditing: "пустой" flush не двигает @LastModifiedDate
- Тема: Нюансы (Auditing)
- Difficulty: 4
- Что нужно сделать: Добавьте в `ProductVersioningService` (бина `productVersioningService`, см. задание 03) метод `void loadAndFlushProduct(Long id)`: в своей транзакции — `em.find(id)` и `em.flush()`, ничего НЕ изменяя. Заранее созданный тест (TASK04Test) фиксирует `updated_at` до вызова (native-запрос), вызывает ваш метод, снова читает `updated_at` — и проверяет, что `@LastModifiedDate` обновляется только когда в БД реально ушёл UPDATE: после "пустого" flush значение не изменилось, а `version` остался 0.
- Стартовый код:
```java
// com/example/jpalab/service/ProductVersioningService.java

// TODO: в своей транзакции: em.find(id); em.flush();  (без изменений сущности)
public void loadAndFlushProduct(Long id) {
    // TODO
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t12.TASK04Test#noOpFlushDoesNotBumpUpdatedAt
```
- Критерий приёмки: Тест: после сохранения `updated_at` заполнен; вызов `loadAndFlushProduct(id)` не бросает исключений; `updated_at` до и после "пустого" flush совпадает ТОЧНО, `version = 0`.
- Подсказка (не решение): аудит-интерцепторы Hibernate срабатывают в момент фактического INSERT/UPDATE, а не при каждом `flush`; без dirty-сущностей flush — пустая операция.
