# Задание 01. Первый программный запрос: точное совпадение по имени

- Тема: Criteria API: программные запросы
- Difficulty: 1
- Что нужно сделать: В `ProductCriteriaService` добавьте метод
  `findByName(String name)`, возвращающий `List<Product>`: постройте
  `CriteriaQuery<Product>` через `CriteriaBuilder` (Root по `Product`),
  предикат точного совпадения `name`, выполните запрос через `EntityManager`
  и верните список сущностей.
- Стартовый код:
```java
public class ProductCriteriaService {
    // TODO: CriteriaQuery<Product> + предикат eq на name
    public List<Product> findByName(String name)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t14.TASK01Test#criteriaFindByNameIsExactMatch
```
- Критерий приёмки: `findByName("Widget B")` находит ровно один товар;
  `findByName("Widget")` возвращает пустой список — совпадение точное.
- Подсказка (не решение): `cb.equal(root.get("name"), name)` — атомарный предикат для `q.where(...)`.
