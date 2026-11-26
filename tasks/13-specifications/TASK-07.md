# Задание 07. Динамический поиск: три опциональных фильтра

- Тема: Specification: динамические предикаты
- Difficulty: 7
- Что нужно сделать: В `ProductSpecifications` добавьте статический метод
  `searchByNameAndPriceAndStock(String nameFragment, BigDecimal minPrice, Integer maxStock)`,
  возвращающий `Specification<Product>`: каждый аргумент `null` — его фильтр выключен
  (предикат «всегда верен»); иначе применяются: `name` содержит `nameFragment`
  (без учёта регистра), `price >= minPrice`, `stockQuantity <= maxStock`.
  Активные фильтры объединяются через `and`.
- Стартовый код:
```java
public class ProductSpecifications {
    // TODO: три независимых опциональных предиката, объединённых and
    public static Specification<Product> searchByNameAndPriceAndStock(String nameFragment, BigDecimal minPrice, Integer maxStock)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t13.TASK07Test#allFiltersAreOptional
```
- Критерий приёмки: все `null` — все 3 товара; каждый фильтр по отдельности —
  свою подмножество; все три вместе (`"WID"`, `50.00`, `50`) — ровно «Widget Two».
- Подсказка (не решение): для выключенного фильтра удобно `Specification.where(pred).and(...)`
  или явный «всегда верный» предикат — выбирайте один подход и держите его во всех трёх.
