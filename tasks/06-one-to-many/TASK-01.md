# Задание 01. Первый OneToMany: каскад сохранения коллекции
- Тема: @OneToMany (Category → products)
- Difficulty: 1
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `Category persistCategoryGraph(Category category)` — в своей транзакции сохранить ТОЛЬКО переданную категорию (один раз); у категории уже привязаны товары с обратной ссылкой (`product.setCategory(category)` + `category.getProducts().add(...)`). Каскад на коллекции (`cascade = ALL`) должен сохранить и товары. Метод возвращает сохранённую категорию. Заранее созданный тест (TASK01Test) строит граф в памяти (категория + 2 товара, без сохранений) и проверяет, что один вызов записал всё: 3 INSERT-а.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: один save/persist категории (каскад сохранит и коллекцию) -> вернуть её
Category persistCategoryGraph(Category category);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK01Test#cascadePersistsCollection
```
- Критерий приёмки: Тест: после вызова у категории id не null; ровно 3 prepared statement и 3 entity INSERT (1 `insert into categories` + 2 `insert into products`); в БД 2 товара, оба с `category_id` этой категории; таблица категорий — 1 строка.
- Подсказка (не решение): `CascadeType.ALL` на `@OneToMany` включает PERSIST/MERGE: сохранение родителя распространяется на managed-коллекцию; владелец FK — ManyToOne, поэтому `category_id` в INSERT-ах товаров заполняется по обратной ссылке.
