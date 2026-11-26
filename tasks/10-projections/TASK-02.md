# Задание 02. Open interface projection через @Query (существующий интерфейс)
- Тема: Проекции
- Difficulty: 2
- Что нужно сделать: В `ProductRepository` добавьте JPQL-метод `List<ProductSummary> summariesInStockAtLeast(@Param("minStock") int minStock)`, использующий СУЩЕСТВУЮЩИЙ интерфейс `ProductRepository.ProductSummary` (getter'ы getName, getPrice, getStockQuantity). Запрос выбирает скалярные поля name, price, stockQuantity для товаров с остатком НЕ МЕНЬШЕ `:minStock` (граница включена), сортировка по имени по возрастанию.
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Query — SELECT p.name, p.price, p.stockQuantity, stockQuantity >= :minStock,
//       ORDER BY p.name ASC; интерфейс ProductSummary уже создан
List<ProductSummary> summariesInStockAtLeast(@Param("minStock") int minStock);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t10.TASK02Test#openInterfaceProjectionViaQuery
```
- Критерий приёмки: Тест: 3 товара (stock 0/5/5), вызов с minStock = 5 — ровно 2 объекта-прокси (B, C по имени), у каждого getName/getPrice/getStockQuantity заполнены (не null), товар A (stock 0) отсутствует.
- Подсказка (не решение): Spring Data сопоставляет имена столбцов SELECT со свойствами интерфейса — для каждого поля нужен алиас, совпадающий с именем свойства.
