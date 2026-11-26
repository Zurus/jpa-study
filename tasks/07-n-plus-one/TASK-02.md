# Задание 02. Измерить N+1 на @OneToMany
- Тема: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
- Difficulty: 2
- Что нужно сделать: Реализуйте в `ProductService` метод `Map<String, Integer> countProductsPerCategory()` — «наивный» сценарий: загрузить ВСЕ категории обычным `findAll()` и в цикле вызвать `category.getProducts().size()`; вернуть map «имя категории -> число товаров». Заранее созданный тест (TASK02Test) вызывает ваш метод и считает prepared statements: 1 SELECT по категориям + по одному на каждую LAZY-коллекцию (1 + N); размер коллекции НЕ влияет на число запросов.
- Стартовый код:
```java
// com/example/jpalab/service/ProductService.java

// TODO: categoryRepository.findAll() + цикл: c.getProducts().size()
//       -> Map<имя категории, размер коллекции>
Map<String, Integer> countProductsPerCategory();
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t07.TASK02Test#measureNPlus1OnCollections
```
- Критерий приёмки: Тест: 4 категории с числом товаров 3/2/1/1; ваш метод возвращает корректную map; всего 5 prepared statement (1 + 4 — по одному на категорию, хотя у первой 3 товара, а у четвёртой 1); в SQL-логе 4 отдельных `select ... from products where category_id = ?`; в комментарии зафиксировано: N+1 для коллекций — это N загрузок коллекций, а не N×размер.
- Подсказка (не решение): LAZY-коллекция инициализируется одним запросом на РОДИТЕЛЯ, а не на элемент.
