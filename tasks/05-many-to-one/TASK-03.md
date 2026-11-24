# Задание 03. Отвязка: category = null
- Тема: @ManyToOne (Product → Category)
- Difficulty: 3
- Что нужно сделать: Тест создан заранее (TASK03Test): товар в категории A. Выполните `product.setCategory(null)` и flush. Убедитесь, что FK в БД стал NULL и Hibernate корректно обработал null-значное навигационное поле (без NPE и без падения flush).
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t05.TASK03Test
// product в A; setCategory(null); flush; SELECT category_id -> NULL;
// повторная загрузка (em.clear + find) — без ошибки
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK03Test#detachFromCategory
```
- Критерий приёмки: Тест: после flush `category_id = NULL`; после `em.clear()` и повторной загрузки `product.getCategory()` — null (не прокси, не ошибка); в SQL-логе `update products set ... category_id = null ...` (Hibernate пишет NULL-параметр, а не `= null`-текст); повторное чтение сущности проходит без исключений.
- Подсказка (не решение): nullable-колонка FK позволяет null; Hibernate различает «поле не трогай» и «поставь null» по ссылке на объект.
