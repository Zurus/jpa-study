# Задание 03. Orphan removal: выкинули из коллекции — строка удалена
- Тема: @OneToMany (Category → products)
- Difficulty: 3
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `void removeProductFromCategory(Long categoryId, String productName)` — в своей транзакции загрузить managed-категорию и убрать товар с именем `productName` ТОЛЬКО ИЗ ЕЁ КОЛЛЕКЦИИ (`getProducts().remove(...)`); `em.remove`/`delete` вызывать НЕ нужно — на коллекции `Category.products` стоит `orphanRemoval = true` (неявно влечёт cascade REMOVE). Заранее созданный тест (TASK03Test): категория с 2 товарами; после вашего метода в БД один DELETE, в категории остался 1 товар, товара с таким именем в таблице нет.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: find(category) -> найти товар по имени в getProducts() ->
//       getProducts().remove(product)  (без em.remove!)
void removeProductFromCategory(Long categoryId, String productName);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK03Test#orphanRemoval
```
- Критерий приёмки: Тест: данные — категория с товарами "Keep" и "RemoveMe"; после вызова ровно 1 prepared statement и 1 entity DELETE (`delete from products where id = ?`); в БД 1 товар с этим `category_id`; товара "RemoveMe" в таблице `products` нет.
- Подсказка (не решение): orphanRemoval: управляемый элемент, вычеркнутый из managed-коллекции, становится «сиротой» и удаляется при flush; отдельного remove ему не положено.
