# Задание 01. Измерить N+1 на @ManyToOne
- Тема: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
- Difficulty: 1
- Что нужно сделать: Тест создан заранее (TASK01Test): 5 товаров, 5 разных категорий. Все товары загружаются `findAll()`, в цикле читается `product.getCategory().getName()`, число prepared statements считается Statistics-ом — показано, что запросов 1 + N, а не 1. Методы в репозиторий добавлять не нужно.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t07.TASK01Test
// Ключевые шаги:
//   5 товаров / 5 категорий; em.flush(); em.clear();
//   statistics.clear();
//   List<Product> products = productRepository.findAll();   // 1 SELECT
//   for (Product p : products) p.getCategory().getName();   // +5 SELECT
//   delta == 5
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t07.TASK01Test#measureNPlus1OnManyToOne
```
- Критерий приёмки: Тест: `findAll` — ровно 1 prepared statement; до цикла все категории НЕ инициализированы (LAZY); цикл доступа к категориям добавляет ровно N (5) SELECT-ов; общее число зафиксировано ассертом; в комментарии к тесту описана схема «1+N»: одна SELECT по таблице сущностей + по одной SELECT на каждую LAZY-ассоциацию.
- Подсказка (не решение): без `em.clear()` после создания данных N+1 не видно — persistence context уже «знает» все объекты.
