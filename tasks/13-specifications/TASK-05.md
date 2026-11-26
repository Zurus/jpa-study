# Задание 05. Комбинатор or: имя ИЛИ остаток

- Тема: Specification: динамические предикаты
- Difficulty: 5
- Что нужно сделать: В `ProductSpecifications` добавьте статический метод
  `nameContainingOrStockAtMost(String fragment, Integer maxStock)`,
  возвращающий `Specification<Product>`: товар подходит, если `name` содержит
  `fragment` без учёта регистра ИЛИ `stockQuantity <= maxStock` — достаточно
  одного из условий.
- Стартовый код:
```java
public class ProductSpecifications {
    // TODO: or из предиката по имени и предиката по остатку
    public static Specification<Product> nameContainingOrStockAtMost(String fragment, Integer maxStock)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t13.TASK05Test#orMatchesEitherPredicate
```
- Критерий приёмки: `("ONE", 10)` — «Alpha One» (по имени) и «Beta» (по остатку),
  «Alpha Two» исключён; `("ZZZ", 10)` — только ветка по остатку;
  `("BETA", 4)` — товар, подходящий по обоим условиям.
- Подсказка (не решение): `cb.or(p1, p2)` строит дизъюнцию двух готовых предикатов
  в одном lambda-теле спецификации.
