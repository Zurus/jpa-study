# Задание 02. Добавление товара в managed-коллекцию
- Тема: @OneToMany (Category → products)
- Difficulty: 2
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `void addProductToCategory(Long categoryId, String productName, BigDecimal price, int stock)` — в своей транзакции: загрузить managed-категорию, создать НОВЫЙ товар (без явного `setCategory` у товара — достаточно `category.getProducts().add(product)`), сохранить. Заранее созданный тест (TASK02Test) проверяет: в БД появился один INSERT товара (без UPDATE-а категории), `category_id` у товара установлен, managed-коллекция содержит товар.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: find(category) -> new product -> category.getProducts().add(product) -> save
void addProductToCategory(Long categoryId, String productName,
                          BigDecimal price, int stock);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK02Test#addToManagedCollection
```
- Критерий приёмки: Тест: данные — пустая категория; вызов с товаром (имя/цена/сток из теста) — ровно 1 prepared statement и 1 entity INSERT (UPDATE-ов ноль); в БД 1 товар с `category_id` этой категории; в памяти у managed-категории `getProducts()` — 1 элемент.
- Подсказка (не решение): при добавлении в managed-коллекцию «владельцем» остаётся ManyToOne: Hibernate сам пропишет `category_id` INSERT-ом, каскад на коллекции не требуется.
