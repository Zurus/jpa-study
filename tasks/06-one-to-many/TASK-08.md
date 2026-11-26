# Задание 08. Двойная «владимость»: коллекция против FK
- Тема: @OneToMany (Category → products)
- Difficulty: 8
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `Category saveCategoryWithBorrowedProduct(String categoryName, Long productId)` — в своей транзакции: создать НОВУЮ категорию с именем `categoryName`, добавить в её коллекцию существующий товар по id БЕЗ вызова `product.setCategory(...)` (т.е. только «запретить» через коллекцию), сохранить категорию и вернуть её. Заранее созданный тест (TASK08Test) показывает ловушку: в памяти новая категория «содержит» товар, но FK остаётся у прежней категории, а после `em.refresh` коллекция оказывается пуста.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: new Category(name) -> find(product) ->
//       category.getProducts().add(product)  // БЕЗ setCategory!
//       -> save(category) -> return category
Category saveCategoryWithBorrowedProduct(String categoryName, Long productId);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK08Test#doubleOwnership
```
- Критерий приёмки: Тест: данные — товар "DoubleOwned" в категории "TrueOwner" (A); ваш метод создаёт "NewClaimant" (C); после flush `category_id` товара в БД = id(A) — FK не переехал; в памяти `c.getProducts()` — 1 элемент («думает», что товар в ней); после `em.refresh(c)` коллекция пуста — по FK товар C не принадлежит.
- Подсказка (не решение): «запрет» через коллекцию без обратной ссылки — только навигационный эффект внутри PC; источник правды для `category_id` — ManyToOne-сторона.
