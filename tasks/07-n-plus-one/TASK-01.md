# Задание 01. Измерить N+1 на @ManyToOne
- Тема: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
- Difficulty: 1
- Что нужно сделать: Реализуйте в `ProductService` метод `List<String> listProductsWithCategoryNames()` — «наивный» сценарий: загрузить ВСЕ товары обычным `findAll()` и в цикле собрать строки вида `"<name> [<categoryName>]"` (категория LAZY — обращение к имени триггерит догрузку). Заранее созданный тест (TASK01Test) вызывает ваш метод и считает prepared statements: 1 SELECT по товарам + по одному на каждую категорию (1 + N).
- Стартовый код:
```java
// com/example/jpalab/service/ProductService.java

// TODO: findAll() + цикл: name + " [" + category.getName() + "]"
List<String> listProductsWithCategoryNames();
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t07.TASK01Test#measureNPlus1OnManyToOne
```
- Критерий приёмки: Тест: 5 товаров / 5 категорий; вызов вашего метода — ровно 6 prepared statement (1 SELECT по товарам + 5 по категориям); до цикла все категории не инициализированы; собранные строки совпадают с ожидаемыми; в комментарии к тесту описана схема «1+N»: одна SELECT по таблице сущностей + по одной SELECT на каждую LAZY-ассоциацию.
- Подсказка (не решение): без `em.clear()` после создания данных N+1 не видно — persistence context уже «знает» все объекты; метод должен быть `@Transactional(readOnly = true)`.
