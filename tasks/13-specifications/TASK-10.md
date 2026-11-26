# Задание 10. Полная динамическая спецификация

- Тема: Specification: динамические предикаты
- Difficulty: 10
- Что нужно сделать: В `ProductSpecifications` добавьте статический метод
  `search(BigDecimal priceFrom, BigDecimal priceTo, String categoryName, Integer minStock)`,
  возвращающий `Specification<Product>` — универсальный динамический фильтр:
  диапазон цены `[priceFrom, priceTo]` (оба конца включаются, любой может быть `null` —
  сторона диапазона «открыта»); `categoryName` — точное имя категории (`null` — фильтр
  по категории выключен, товары без категории должны оставаться в результате);
  `minStock` — `stockQuantity >= minStock` (`null` — выключен). Все активные фильтры
  объединяются через `and`; все аргументы `null` — возвращаются все строки.
- Стартовый код:
```java
public class ProductSpecifications {
    // TODO: до 4 независимых опциональных предикатов (диапазон цены = 2 предиката)
    public static Specification<Product> search(BigDecimal priceFrom, BigDecimal priceTo, String categoryName, Integer minStock)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t13.TASK10Test#openEndedRangeAndCategory
```
- Критерий приёмки: все `null` — все 4 товара, включая товар без категории;
  только `priceFrom = 50.00` — 3 товара (50.00 включено); только `priceTo = 50.00` — 3 товара;
  `(50.00, 50.00, "Beta", 10)` — ровно «Beta 1»; `("Alpha")` — 2 товара Alpha,
  товар без категории исключён (фильтр по категории включён); только `minStock = 10` —
  2 товара с остатком не меньше 10.
- Подсказка (не решение): соберите список активных `Predicate` и передайте его в
  `cb.and(...)` массивом; фильтр по категории добавляйте (и join к `category` стройте)
  только когда `categoryName != null`.
