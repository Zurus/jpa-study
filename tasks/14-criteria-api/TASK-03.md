# Задание 03. Выборка Path: имена вместо сущностей

- Тема: Criteria API: программные запросы
- Difficulty: 3
- Что нужно сделать: В `ProductCriteriaService` добавьте метод
  `findNamesByStockAtLeast(int minStock)`, возвращающий `List<String>`:
  запрос с `CriteriaQuery<String>` — в выборке `Path` (имя товара), а не
  целая сущность; условие `stockQuantity >= minStock` (граница включается).
- Стартовый код:
```java
public class ProductCriteriaService {
    // TODO: CriteriaQuery<String>, select по Path name
    public List<String> findNamesByStockAtLeast(int minStock)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t14.TASK03Test#selectsPathNotEntity
```
- Критерий приёмки: результат — список строк (не `Product`): при `5` —
  «Few» и «Many» (товар с остатком 0 не входит); при `0` — все три имени.
- Подсказка (не решение): тип `CriteriaQuery` задаётся при `cb.createQuery(String.class)`,
  а `q.select(root.get("name"))` возвращает скаляр.
