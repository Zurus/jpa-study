# Задание 06. Устаревшая коллекция и refresh
- Тема: @OneToMany (Category → products)
- Difficulty: 6
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `int refreshCategoryProductCount(Long categoryId)` — в своей транзакции загрузить managed-категорию, принудительно перечитать её коллекцию из БД (`em.refresh(category)`) и вернуть свежее число товаров. Заранее созданный тест (TASK06Test): категория с 1 товаром загружена в PC, затем «в обход» (native-INSERT) в БД появился второй товар; managed-коллекция по-прежнему «видит» 1 — ваш метод через refresh приводит её в соответствие с БД и возвращает 2.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: find(category) -> em.refresh(category) -> return getProducts().size()
int refreshCategoryProductCount(Long categoryId);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK06Test#staleCollectionAfterSideInsert
```
- Критерий приёмки: Тест: данные — категория с 1 товаром; до refresh `managed.getProducts().size() == 1`, а в БД уже 2 (native-INSERT мимо PC); ваш метод возвращает 2; после него `managed.getProducts().size() == 2` — коллекция перечитана из БД.
- Подсказка (не решение): managed-коллекция — это кэш в persistence context: изменения, сделанные мимо JPA, не видны до `refresh` (или до очистки PC).
