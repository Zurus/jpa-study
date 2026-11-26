# Задание 10. Один и тот же запрос в двух формах: record против интерфейса
- Тема: Проекции
- Difficulty: 10
- Что нужно сделать: (1) Создайте интерфейс-проекцию `NamePriceView` в пакете `com.example.jpalab.dto` с getter'ами `String getName()`, `BigDecimal getPrice()`. (2) В `ProductRepository` добавьте ДВА метода с ОДИНАКОВОЙ логикой: товары с ценой НЕ МЕНЬШЕ `:minPrice` (граница включена), сортировка по имени ASC, тай-брейк id ASC: (a) `List<ProductNamePrice> namePricePairsForPricedAtLeast(...)` — конструкторное выражение в существующий record `ProductNamePrice`; (b) `List<NamePriceView> namePricePairsAsInterface(...)` — скалярный SELECT (name, price) с алиасами, совпадающими со свойствами интерфейса.
- Стартовый код:
```java
// src/main/java/com/example/jpalab/dto/NamePriceView.java (новый файл)

public interface NamePriceView {
    String getName();
    BigDecimal getPrice();
}
```
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: два метода, одна и та же логика — price >= :minPrice,
//       ORDER BY p.name ASC, p.id ASC:
List<ProductNamePrice> namePricePairsForPricedAtLeast(@Param("minPrice") BigDecimal minPrice);
List<NamePriceView> namePricePairsAsInterface(@Param("minPrice") BigDecimal minPrice);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t10.TASK10Test#sameQueryRecordVsInterface
```
- Критерий приёмки: Тест: 4 товара (100.00, 200.00, 300.00, 50.00), вызов с 100.00 — обе выборки содержат ровно A, B, C в порядке имён (D с 50.00 отсутствует); record-список — value-объекты (isRecord = true, equals по содержимому), интерфейс-список — JDK-прокси (не record).
- Подсказка (не решение): для open interface «собиратель» объекта — это имена столбцов (алиасы), а для конструкторного выражения — порядок аргументов; логика обоих методов должна совпадать посимвольно по WHERE/ORDER BY.
