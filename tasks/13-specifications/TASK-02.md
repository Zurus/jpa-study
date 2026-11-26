# Задание 02. Comparison-предикат: минимальная цена

- Тема: Specification: динамические предикаты
- Difficulty: 2
- Что нужно сделать: В `ProductSpecifications` добавьте статический метод
  `byPriceAtLeast(BigDecimal minPrice)`, возвращающий `Specification<Product>`:
  `price` больше или равно `minPrice` (граница включается).
- Стартовый код:
```java
public class ProductSpecifications {
    // TODO: нестрогое сравнение price >= minPrice
    public static Specification<Product> byPriceAtLeast(BigDecimal minPrice)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t13.TASK02Test#priceAtLeastIsInclusive
```
- Критерий приёмки: при `minPrice = 50.00` товар с ценой ровно 50.00 входит в результат;
  при `50.01` — не входит.
- Подсказка (не решение): у `CriteriaBuilder` есть готовые сравнения `gt`/`gte`/`lt`/`lte`.
