# Задание 02. Измерить N+1 на @OneToMany
- Тема: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
- Difficulty: 2
- Что нужно сделать: Тест создан заранее (TASK02Test): 4 категории с разным числом товаров (3, 2, 1, 1). Все категории загружаются, в цикле вызывается `category.getProducts().size()`. Получилось 1 + N запросов; зафиксировано, что размер коллекции НЕ влияет на число запросов. Методы в репозиторий добавлять не нужно.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t07.TASK02Test
// Ключевые шаги:
//   категории 3/2/1/1; em.flush(); em.clear();
//   statistics.clear();
//   List<Category> cats = categoryRepository.findAll();      // 1
//   for (Category c : cats) c.getProducts().size();          // +4
//   delta == 4
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t07.TASK02Test#measureNPlus1OnCollections
```
- Критерий приёмки: Тест: `findAll` категорий — 1 statement; цикл `size()` — ровно 4 (по одному на категорию, независимо от того, что у первой 3 товара, а у четвёртой 1); в SQL-логе 4 отдельных `select ... from products where category_id = ?`; в комментарии зафиксировано: N+1 для коллекций — это N загрузок коллекций, а не N×размер.
- Подсказка (не решение): LAZY-коллекция инициализируется одним запросом на РОДИТЕЛЯ, а не на элемент.
