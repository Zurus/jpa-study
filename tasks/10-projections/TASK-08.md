# Задание 08. Ловушка: порядок аргументов конструкторного выражения
- Тема: Проекции
- Difficulty: 8
- Что нужно сделать: (1) Создайте record в пакете `com.example.jpalab.dto` ИМЕННО в этом порядке компонентов: `public record PriceStockAndName(BigDecimal price, int stockQuantity, String name) {}` (price, stockQuantity, name — не «естественный» порядок). (2) В `ProductRepository` добавьте JPQL-метод `List<PriceStockAndName> priceStockNameForPricedBelow(@Param("maxPrice") BigDecimal maxPrice)` — конструкторное выражение для товаров со СТРОГО меньшей ценой (товар с ценой ровно равной границе НЕ попадает). Сортировка — по имени товара по возрастанию.
- Стартовый код:
```java
// src/main/java/com/example/jpalab/dto/PriceStockAndName.java (новый файл)

public record PriceStockAndName(BigDecimal price, int stockQuantity, String name) {
}
```
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Query — конструкторное выражение в PriceStockAndName,
//       price < :maxPrice (строго), ORDER BY p.name ASC
List<PriceStockAndName> priceStockNameForPricedBelow(@Param("maxPrice") BigDecimal maxPrice);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t10.TASK08Test#constructorArgumentOrderMatters
```
- Критерий приёмки: Тест: 4 товара (30.00/5, 70.00/3, 90.00/1, 100.00/9), вызов с maxPrice = 100.00 — ровно 3 строки (D с ценой ровно 100.00 отсутствует); у каждой строки price/stockQuantity/name совпадают по номиналу (перепутанный порядок аргументов конструктора сразу виден по полям).
- Подсказка (не решение): аргументы в `SELECT new ...` обязаны совпадать с ПОРЯДОК компонентов record — сверьте типы по позициям.
