# Задание 05. Коллекция без обратной ссылки: FK не двигается
- Тема: @OneToMany (Category → products)
- Difficulty: 5
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `void addProductToCollectionOnly(Long targetCategoryId, Long productId)` — в своей транзакции загрузить managed-категорию и добавить товар (без `setCategory`) только в её коллекцию: `category.getProducts().add(product)`. Заранее созданный тест (TASK05Test) доказывает: изменение «одной» стороны (коллекции) без обновления «владеющей» стороны (ManyToOne) НИЧЕГО не пишет в БД по товару — ни UPDATE, ни INSERT; FK остаётся у прежней категории.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: find(category) -> find(product) ->
//       category.getProducts().add(product)   // БЕЗ product.setCategory!
void addProductToCollectionOnly(Long targetCategoryId, Long productId);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK05Test#collectionAloneDoesNotMoveFk
```
- Критерий приёмки: Тест: данные — товар в категории "RealOwner" (A), существует "WantsToOwn" (B); после вызова: 0 entity UPDATE и 0 entity INSERT по товару; в БД `category_id` товара = id(A) — коллекция B «думает» о товаре, но FK не поменялся.
- Подсказка (не решение): владелец FK — `@ManyToOne`; «невладельческая» коллекция — лишь навигация, её изменение само по себе UPDATE-а `category_id` не порождает.
