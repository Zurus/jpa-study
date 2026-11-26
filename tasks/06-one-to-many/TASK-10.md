# Задание 10. Откат транзакции: INSERT-ы коллекции отменяются
- Тема: @OneToMany (Category → products)
- Difficulty: 10
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `void addTwoProductsToCategory(Long categoryId)` — в транзакции (простой `@Transactional`, REQUIRED — вливается в транзакцию вызывающего кода) загрузить managed-категорию и добавить в её коллекцию ДВА новых товара (с уникальными именами; обратная ссылка `setCategory` + коллекция). Заранее созданный тест (TASK10Test, `@SpringBootTest` + `TransactionTemplate`): setup — закоммиченная пустая категория; затем транзакция вызывает ваш метод, делает `em.flush()` (INSERT-ы выполнены) и завершается ОТКАТОМ; проверяется, что в БД товаров нет, а свежая коллекция пуста.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: @Transactional: find(category) -> 2 x (new product + add + setCategory)
void addTwoProductsToCategory(Long categoryId);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK10Test#rollbackUndoesCollectionInserts
```
- Критерий приёмки: Тест: в откатываемой транзакции ваш метод + `em.flush()` (INSERT-ы сгенерированы) + принудительный `RuntimeException`; в новой транзакции `SELECT COUNT(*) FROM products WHERE category_id = ?` = 0 и свежая `category.getProducts()` пуста — откат отменил и каскадные INSERT-ы коллекции.
- Подсказка (не решение): INSERT-ы, сделанные каскадом, — те же самые UPDATE/INSERT транзакции: rollback стирает и их вместе со «владельцем».
