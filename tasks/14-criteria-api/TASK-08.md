# Задание 08. Агрегат SUM(price) по категории

- Тема: Criteria API: программные запросы
- Difficulty: 8
- Что нужно сделать: В `ProductCriteriaService` добавьте метод
  `sumPricesInCategory(String categoryName)`, возвращающий `BigDecimal`:
  агрегатная выборка `cb.sum(root.get("price"))` (тип запроса
  `CriteriaQuery<BigDecimal>`) с `Join` по категории и предикатом на имя;
  категория без товаров вернёт `null` (SQL-семантика агрегата по пустому
  набору). Результат берите через `getSingleResult()`.
- Стартовый код:
```java
public class ProductCriteriaService {
    // TODO: CriteriaQuery<BigDecimal> + cb.sum + join-категория
    public BigDecimal sumPricesInCategory(String categoryName)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t14.TASK08Test#sumPricesInCategory
```
- Критерий приёмки: «Alpha» — 30.75 (10.50 + 20.25); «Beta» — 999.99;
  «Gamma» (нет товаров) — `null`; товар без категории в сумму не входит.
- Подсказка (не решение): `q.select(cb.sum(root.get("price")))` — выборка сама
  по себе агрегат, `getSingleResult()` вернёт одно скалярное значение.
