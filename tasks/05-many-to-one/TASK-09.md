# Задание 09. Merge отстёгнутого товара со сменой категории
- Тема: @ManyToOne (Product → Category)
- Difficulty: 9
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `Product mergeDetachedProductWithCategory(Product detached, String categoryName)`: в своей транзакции найти существующую категорию по имени, установить её на переданный DETACHED-товар (`detached.setCategory(category)`) и выполнить `productRepository.save(detached)` (merge), вернув managed-результат. Заранее созданный тест (TASK09Test): товар загружен (категория A), persistence context очищен, у detached-копии через ваш метод меняется категория на B; проверяется, что FK обновился, версия увеличилась, а merge вернул managed-копию (не ту, что передали).
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: find category by name -> detached.setCategory(category) ->
//       return productRepository.save(detached)
Product mergeDetachedProductWithCategory(Product detached, String categoryName);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK09Test#mergeDetachedWithNewCategory
```
- Критерий приёмки: Тест: результат вашего метода — другая инстанция, чем переданная (isNotSameAs); `version` увеличен на 1; в БД `category_id = B` и одна строка товара (не две); в SQL-логе `update products` с `category_id` и `where id = ? and version = ?`; в комментарии зафиксировано: merge копирует состояние (включая FK) в managed-инстанцию — «отстёгнутый» объект по-прежнему detached.
- Подсказка (не решение): merge — это «применить diff отстёгнутого к управляемому»: навигационные поля копируются вместе со скалярами.
