# Задание 04. Join по категории (inner)

- Тема: Criteria API: программные запросы
- Difficulty: 4
- Что нужно сделать: В `ProductCriteriaService` добавьте метод
  `findByCategoryName(String categoryName)`, возвращающий `List<Product>`:
  товары заданной категории — `Join` от `Product` к `category` и предикат
  по имени на join-пути. Товар без категории в результат не попадает
  (inner-join).
- Стартовый код:
```java
public class ProductCriteriaService {
    // TODO: root.join("category") + предикат по имени категории
    public List<Product> findByCategoryName(String categoryName)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t14.TASK04Test#innerJoinByCategoryName
```
- Критерий приёмки: «Alpha» — два товара Alpha; «Beta» — один; «Gamma» — пусто;
  товар «Free» (без категории) отсутствует во всех результатах.
- Подсказка (не решение): `root.join("category")` создаёт `Join<Product, Category>`;
  предикат строите по `join.get("name")`, а выбираете `root`.
