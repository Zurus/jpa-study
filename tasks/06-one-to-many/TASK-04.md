# Задание 04. Перенос товара между категориями
- Тема: @OneToMany (Category → products)
- Difficulty: 4
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `void moveProductToCategory(Long productId, Long targetCategoryId)` — в своей транзакции перенести товар: убрать его из коллекции текущей категории, добавить в коллекцию целевой категории и обновить обратную ссылку (`product.setCategory(target)`). Заранее созданный тест (TASK04Test) проверяет: ровно один UPDATE по товару (смена `category_id`), без INSERT/DELETE, строка товара одна и та же.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: find(product); от его категории: getProducts().remove(product);
//       target.getProducts().add(product); product.setCategory(target)
void moveProductToCategory(Long productId, Long targetCategoryId);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK04Test#moveBetweenCategories
```
- Критерий приёмки: Тест: данные — товар "Mover" в категории "From", существует категория "To"; после вызова: ровно 1 entity UPDATE (смена FK), 0 INSERT и 0 DELETE (товар не «пересоздавался» и не становился сиротой); в БД `category_id` = id("To"), в таблице `products` ровно 1 строка.
- Подсказка (не решение): сирота появляется, если товар вычеркнули из managed-коллекции И НЕ добавили в другую до flush; порядок операций внутри одной транзакции решает исход.
