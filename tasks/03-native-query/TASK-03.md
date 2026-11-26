# Задание 03. Проекция в Object[]
- Тема: Native queries
- Difficulty: 3
- Что нужно сделать: Добавьте в `ProductRepository` native-метод `List<Object[]> nativeIdNamePriceAbove(@Param("minPrice") BigDecimal minPrice)`: `@Query(nativeQuery = true)`, возвращающий колонки id, name, price (порядок именно такой) для товаров, цена которых строго больше `:minPrice`, отсортированных по имени. Заранее созданный тест (TASK03Test) вызывает этот метод и вручную маппит строки в record `ProductNamePrice`.
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Query(nativeQuery = true) — SELECT id, name, price FROM products
//       WHERE price > :minPrice ORDER BY name
List<Object[]> nativeIdNamePriceAbove(@Param("minPrice") BigDecimal minPrice);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t03.TASK03Test#objectArrayRowsMapManuallyToRecord
```
- Критерий приёмки: Тест: каждая строка — массив из 3 элементов (Long, String, BigDecimal); ручной маппинг в record `ProductNamePrice` даёт ожидаемые пары; порядок строк — по имени; в SQL-логе выбраны ровно три колонки.
- Подсказка (не решение): `Object[]` — универсальный контейнер для проекции, порядок элементов = порядок колонок в SELECT; в native-запросе пишите имена КОЛОНОК и таблицы, а не свойства и сущности.
