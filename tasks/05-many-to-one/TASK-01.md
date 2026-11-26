# Задание 01. Первый ManyToOne: FK null и FK установлен
- Тема: @ManyToOne (Product → Category)
- Difficulty: 1
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `Product attachCategoryToProduct(Long productId, String categoryName)`: в своей транзакции загрузить товар, при необходимости создать категорию с именем `categoryName` (`findByCategoryName` или `save`), установить `product.setCategory(category)` и вернуть обновлённую сущность. Заранее созданный тест (TASK01Test) сохраняет товар БЕЗ категории (проверяет `category_id = NULL` в БД через native-запрос), затем вызывает ваш метод с именем категории "Cat" и проверяет, что FK установлен.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: find(product) -> find-or-create category(categoryName) ->
//       product.setCategory(category) -> return product
Product attachCategoryToProduct(Long productId, String categoryName);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK01Test#fkLifecycle
```
- Критерий приёмки: Тест: после первого сохранения `SELECT category_id FROM products WHERE id = ?` возвращает NULL; после вызова вашего метода тот же SELECT возвращает id категории; возвращённый товар — с тем же id и с заполненной `getCategory()`; в SQL-логе между шагами виден `update products` с колонкой `category_id`.
- Подсказка (не решение): ManyToOne — «владелец» связи: колонку-ключ он заполняет сам при сохранении/обновлении.
