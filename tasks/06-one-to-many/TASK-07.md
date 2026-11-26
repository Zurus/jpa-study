# Задание 07. Каскад удаления: порядок DELETE-ов
- Тема: @OneToMany (Category → products)
- Difficulty: 7
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `void deleteCategoryWithCascade(Long categoryId)` — в своей транзакции удалить категорию по id; каскад (`CascadeType.ALL` включает REMOVE) должен сам удалить все её товары. Заранее созданный тест (TASK07Test): категория с 3 товарами; после одного удаления категории в БД 4 DELETE-а (сначала товары, затем категория — иначе нарушится FK).
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: find(category) -> em.remove(category)  (каскад унесёт и товары)
void deleteCategoryWithCascade(Long categoryId);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK07Test#cascadeRemoveOrder
```
- Критерий приёмки: Тест: данные — категория с 3 товарами; после вызова: 0 товаров и 0 категорий в БД; `entityDeleteCount` == 4 (3 товара + 1 категория); в SQL-логе DELETE-ы по `products` идут ДО DELETE-а по `categories` (порядок наложен FK-ограничением).
- Подсказка (не решение): cascade REMOVE распространяется по навигации от родителя; порядок «дети раньше родителя» Hibernate выстраивает сам, но это видно в логе.
