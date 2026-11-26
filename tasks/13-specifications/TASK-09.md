# Задание 09. Приоритет and/or: явная группировка

- Тема: Specification: динамические предикаты
- Difficulty: 9
- Что нужно сделать: В `ProductSpecifications` добавьте статический метод
  `namedAndPricedOrScarce(String nameFragment, BigDecimal minPrice, Integer maxStock)`,
  возвращающий `Specification<Product>`: товар подходит, если `name` содержит
  `nameFragment` (без учёта регистра) **и** при этом (`price >= minPrice` **или**
  `stockQuantity <= maxStock`). Ветка «цена ИЛИ остаток» сгруппирована внутри
  условия по имени: без совпадения по имени ни один из двух вторичных фильтров
  товар не включает.
- Стартовый код:
```java
public class ProductSpecifications {
    // TODO: name AND (price OR stock) — группировка OR должна быть явной
    public static Specification<Product> namedAndPricedOrScarce(String nameFragment, BigDecimal minPrice, Integer maxStock)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t13.TASK09Test#precedenceRequiresExplicitGrouping
```
- Критерий приёмки: `("ALPHA", 100.00, 10)` — ровно «Alpha One» (цена) и «Alpha Two»
  (малый остаток); «Beta Two» (имя не совпадает, остаток мал) **исключён** — неверная
  группировка `(name AND price) OR stock` даст его и красный тест.
  `("BETA", 100.00, 10)` — ровно «Beta» и «Beta Two».
- Подсказка (не решение): `and` и `or` равноправны и ассоциируются «слева направо» —
  соберите сначала внутренний `or`, а затем оберните его в `and` с предикатом по имени.
