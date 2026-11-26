# Задание 06. Сортировка: цена DESC, имя ASC (тай-брейк)

- Тема: Criteria API: программные запросы
- Difficulty: 6
- Что нужно сделать: В `ProductCriteriaService` добавьте метод
  `findSortedPriceDescNameAsc(BigDecimal minPrice)`, возвращающий
  `List<Product>`: товары с ценой `>= minPrice`, отсортированные по цене
  по убыванию; при равной цене — по имени по возрастанию (тай-брейк).
  Сортировка задаётся через `CriteriaQuery.orderBy`.
- Стартовый код:
```java
public class ProductCriteriaService {
    // TODO: where(price >= minPrice) + orderBy(цена DESC, имя ASC)
    public List<Product> findSortedPriceDescNameAsc(BigDecimal minPrice)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t14.TASK06Test#priceDescWithNameTieBreak
```
- Критерий приёмки: при `50.00` — строго `["Alpha", "Beta", "Gamma"]`
  (оба товара по 100.00 идут в алфавитном порядке, «Beta» вставлен раньше
  «Alpha»); при `100.00` — строго `["Alpha", "Beta"]`.
- Подсказка (не решение): `q.orderBy(cb.desc(root.get("price")), cb.asc(root.get("name")))` —
  два `Order`-а задают полный детерминированный порядок.
