# Задание 07. Программная сборка динамического WHERE

- Тема: Criteria API: программные запросы
- Difficulty: 7
- Что нужно сделать: В `ProductCriteriaService` добавьте метод
  `findDynamic(List<String> names, BigDecimal minPrice, boolean inStockOnly)`,
  возвращающий `List<Product>`: предикаты собираются программно —
  `names` (список точных имён; `null` или пустой список — фильтр выключен),
  `minPrice` (`null` — выключен; иначе `price >= minPrice`),
  `inStockOnly` (`true` — `stockQuantity > 0`). Активные предикаты
  объединяются в `WHERE` через `and`; при отсутствии активных — все товары.
- Стартовый код:
```java
public class ProductCriteriaService {
    // TODO: сборка списка Predicate по аргументам + where(and)
    public List<Product> findDynamic(List<String> names, BigDecimal minPrice, boolean inStockOnly)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t14.TASK07Test#dynamicWhereIsBuiltProgrammatically
```
- Критерий приёмки: всё выключено — все 4 товара; `["Widget","Bolt"]` — два;
  `minPrice=30.00` — «Gizmo» и «Bolt»; `inStockOnly=true` — три (исключён
  «Gadget» с остатком 0); `["Gadget"] + inStockOnly` — пусто.
- Подсказка (не решение): держите `List<Predicate>` и добавляйте в него
  условно; `root.get("name").in(names)` готовый IN-предикат.
