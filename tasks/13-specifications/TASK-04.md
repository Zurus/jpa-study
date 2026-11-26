# Задание 04. Композиция переиспользуемых спецификаций через and

- Тема: Specification: динамические предикаты
- Difficulty: 4
- Что нужно сделать: В `ProductSpecifications` добавьте статический метод
  `inCategoryWithPriceAtLeast(String categoryName, BigDecimal minPrice)`,
  возвращающий `Specification<Product>`: категория (по имени `category.name`)
  совпадает с `categoryName` И `price >= minPrice` — оба условия одновременно.
  Составьте метод из переиспользуемых предикатов заданий 01–02 через `and`.
- Стартовый код:
```java
public class ProductSpecifications {
    // TODO: категория И цена через and
    public static Specification<Product> inCategoryWithPriceAtLeast(String categoryName, BigDecimal minPrice)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t13.TASK04Test#andCombinesCategoryAndPrice
```
- Критерий приёмки: `("Alpha", 50.00)` — только дорогой товар категории Alpha;
  `("Alpha", 5.00)` — оба товара Alpha; `("Beta", 50.00)` — товар Beta.
- Подсказка (не решение): `Specification.and(Specification)` соединяет два готовых предиката
  без повторной сборки `Predicate`.
