# Задание 10. GROUP BY + HAVING через Criteria

- Тема: Criteria API: программные запросы
- Difficulty: 10
- Что нужно сделать: В `ProductCriteriaService` добавьте метод
  `findCategoryNamesWithAveragePriceAtLeast(BigDecimal minAverage)`,
  возвращающий `List<String>`: имена категорий, где средняя цена товаров
  `AVG(price) >= minAverage` (граница включается). Программная форма:
  выборка по `Path` имени категории, `groupBy` по тому же пути,
  `having` на `cb.avg(...)` (внимание: по JPA-спеке `cb.avg` возвращает
  `Expression<Double>` — сравнивайте с `double`-значением), `orderBy`
  по имени категории (восходящий). Товар без категории группы не образует.
- Стартовый код:
```java
public class ProductCriteriaService {
    // TODO: groupBy(имя категории) + having(avg >= min) + orderBy
    public List<String> findCategoryNamesWithAveragePriceAtLeast(BigDecimal minAverage)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t14.TASK10Test#groupByWithHavingOnAverage
```
- Критерий приёмки: `minAverage=50.00` — `["Alpha", "Gamma"]` (средние 55 и 50,
  граница включается; «Beta» со средней 25 — нет); `25.00` — все три по алфавиту;
  `55.00` — только `["Alpha"]`; дорогой товар без категории не влияет на группы.
- Подсказка (не решение): `having` — это «WHERE по группам»: передайте туда
  сравнение на выражение `cb.avg(...)`, а в `select`/`groupBy` — путь имени.
