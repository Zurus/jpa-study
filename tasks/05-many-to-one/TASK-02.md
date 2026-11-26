# Задание 02. Смена FK: привязка товара к другой категории
- Тема: @ManyToOne (Product → Category)
- Difficulty: 2
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `Product reassignProductCategory(Long productId, String categoryName)`: в своей транзакции загрузить товар, найти существующую категорию по имени (`categoryRepository.findByName`/аналог) и перевести товар в неё одним `setCategory`, вернув сущность. Заранее созданный тест (TASK02Test): товар привязан к категории A; ваш метод переводит его в категорию B; проверяется, что в БД обновился внешний ключ, а строка товара не пересоздавалась (тот же id).
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: find(product) -> find category by name -> setCategory -> return product
Product reassignProductCategory(Long productId, String categoryName);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK02Test#reassignCategory
```
- Критерий приёмки: Тест: после вызова `category_id` равен id категории B; id товара не изменился; в памяти `product.getCategory().getName()` — "B"; в SQL-логе ровно один `update products ... where id = ?` (без delete/insert).
- Подсказка (не решение): изменение навигационного поля managed-сущности — это «грязь», которую dirty checking переведёт в UPDATE.
