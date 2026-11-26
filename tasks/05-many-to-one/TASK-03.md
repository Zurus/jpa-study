# Задание 03. Отвязка: category = null
- Тема: @ManyToOne (Product → Category)
- Difficulty: 3
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `void detachProductFromCategory(Long productId)`: в своей транзакции загрузить товар и выполнить `product.setCategory(null)`. Заранее созданный тест (TASK03Test): товар в категории A; ваш метод отвязывает его; проверяется, что FK в БД стал NULL и Hibernate корректно обработал null-значное навигационное поле (без NPE и без падения flush), а повторная загрузка (свежий persistence context) — без ошибки.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: find(product) -> setCategory(null)
void detachProductFromCategory(Long productId);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK03Test#detachFromCategory
```
- Критерий приёмки: Тест: после вызова `category_id = NULL`; после `em.clear()` и повторной загрузки `product.getCategory()` — null (не прокси, не ошибка); в SQL-логе `update products set ... category_id = null ...` (Hibernate пишет NULL-параметр, а не `= null`-текст).
- Подсказка (не решение): nullable-колонка FK позволяет null; Hibernate различает «поле не трогай» и «поставь null» по ссылке на объект.
