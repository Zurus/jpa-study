# Задание 02. Диапазон цены: два сравнения через and

- Тема: Criteria API: программные запросы
- Difficulty: 2
- Что нужно сделать: В `ProductCriteriaService` добавьте метод
  `findPriceBetween(BigDecimal from, BigDecimal to)`, возвращающий
  `List<Product>`: товары с ценой в диапазоне `[from, to]` (оба конца
  включаются) — два comparison-предиката, объединённых через `CriteriaBuilder.and`.
- Стартовый код:
```java
public class ProductCriteriaService {
    // TODO: price >= from И price <= to через cb.and
    public List<Product> findPriceBetween(BigDecimal from, BigDecimal to)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t14.TASK02Test#priceBetweenIsInclusive
```
- Критерий приёмки: `(50.00, 100.00)` — два товара, включая оба конца диапазона;
  `(0.00, 10.00)` — один; промежуток между ценами `(10.01, 49.99)` — пусто.
- Подсказка (не решение): у `CriteriaBuilder` сравнения `gte`/`lte` принимают
  и параметр (значения) — строите два `Predicate` и соединяете их.
