# Задание 09. Подзапрос: категории с дорогим товаром

- Тема: Criteria API: программные запросы
- Difficulty: 9
- Что нужно сделать: В `ProductCriteriaService` добавьте метод
  `findInCategoriesWithPriceAtLeast(BigDecimal threshold)`, возвращающий
  `List<Product>`: все товары (включая дешёвые) из тех категорий, в которых
  есть хотя бы один товар с ценой `>= threshold`. Решение — подзапрос
  (`CriteriaQuery.subquery`): внутренний запрос выбирает id категорий,
  где `price >= threshold`, внешний — `where(category.id IN (subquery))`.
- Стартовый код:
```java
public class ProductCriteriaService {
    // TODO: subquery по категориям + where(id IN sub)
    public List<Product> findInCategoriesWithPriceAtLeast(BigDecimal threshold)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t14.TASK09Test#subqueryOnExpensiveCategories
```
- Критерий приёмки: `threshold=100.00` — оба товара «Alpha» (дороговата только
  Alpha), при этом «Free» (150.00, без категории) и товары «Beta» исключены;
  `threshold=50.00` — все 4 товара, у которых есть категория.
- Подсказка (не решение): `q.subquery(Long.class)` + свой `from` внутри подзапроса;
  внешний предикат ссылается на `sub` как на `Expression` через `Path.in(sub)`.
