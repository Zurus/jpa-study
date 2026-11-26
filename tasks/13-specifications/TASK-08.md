# Задание 08. Предикат по category.name + Specification и Pageable

- Тема: Specification: динамические предикаты
- Difficulty: 8
- Что нужно сделать: В `ProductSpecifications` добавьте статический метод
  `byCategoryNameContaining(String fragment)`, возвращающий `Specification<Product>`:
  товары, у которых `category.name` содержит `fragment` без учёта регистра
  (предикат по свойству-пути через ассоциацию). В тесте спецификация применяется
  с `Pageable` (`findAll(spec, PageRequest)`) и `count(spec)` — убедитесь, что
  предикат корректно работает и со страницами, и со счётчиком.
- Стартовый код:
```java
public class ProductSpecifications {
    // TODO: предикат по пути category.name (contains, без учёта регистра)
    public static Specification<Product> byCategoryNameContaining(String fragment)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t13.TASK08Test#categoryPathPredicateWithPage
```
- Критерий приёмки: `count(spec("SHELF")) = 3`; страница size=2 с сортировкой по цене
  даёт ровно два первых по цене товара категории «Alpha Shelf»; метаданные
  `totalElements = 3`, `totalPages = 2`, `hasNext = true`; `spec("RACK")` — 1 товар.
- Подсказка (не решение): путь `root.get("category").get("name")` строит implicit-join
  сам; Pageable не требует ничего особенного от спецификации.
