# Задание 05. LEFT JOIN и динамический WHERE

- Тема: Criteria API: программные запросы
- Difficulty: 5
- Что нужно сделать: В `ProductCriteriaService` добавьте метод
  `findByCategoryNameLeftJoin(String categoryName)`, возвращающий
  `List<Product>`: если `categoryName == null` — фильтр по категории
  выключен, возвращаются ВСЕ товары, включая товар без категории;
  иначе — товары категории через `LEFT JOIN` (join строится с
  `JoinType.LEFT`, предикат добавляется только при непустом имени).
- Стартовый код:
```java
public class ProductCriteriaService {
    // TODO: null — все товары; иначе join(JoinType.LEFT) + предикат
    public List<Product> findByCategoryNameLeftJoin(String categoryName)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t14.TASK05Test#leftJoinKeepsOrphanWhenFilterOff
```
- Критерий приёмки: `null` — все 4 товара, в т.ч. «Free» без категории;
  `"Alpha"` — только два товара Alpha.
- Подсказка (не решение): `root.join("category", JoinType.LEFT)` сохраняет строки
  без категории, когда предикат на join не наложен.
