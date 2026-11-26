# Задание 09. Сколько INSERT-ов стоит граф категории
- Тема: @OneToMany (Category → products)
- Difficulty: 9
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `Category createCategoryWithProducts(String categoryName, int productCount)` — в своей транзакции создать категорию и `productCount` товаров в ней (цены/имена любые, но уникальные имена — например, по индексу; обратная ссылка `setCategory` + коллекция), сохранить и вернуть категорию. Заранее созданный тест (TASK09Test) просит создать 1 категорию с 25 товарами и считает SQL: 26 INSERT-ов (1 + 25) — без «удивительных» UPDATE-ов коллекции.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: category + productCount товаров (add в коллекцию + setCategory) ->
//       один save категории -> return category
Category createCategoryWithProducts(String categoryName, int productCount);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK09Test#cascadeInsertsCount
```
- Критерий приёмки: Тест: вызов с 25 товарами — ровно 26 prepared statement и 26 entity INSERT (1 категория + 25 товаров, без UPDATE-ов коллекции); в БД 25 товаров с `category_id` созданной категории.
- Подсказка (не решение): с `@JoinColumn` у «невладельческой» коллекции Hibernate писал бы N UPDATE-ов `category_id`; здесь владелец — ManyToOne, и граф пишется чистыми INSERT-ами.
