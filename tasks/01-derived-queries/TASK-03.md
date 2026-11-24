# Задание 03. Top + OrderBy без предиката
- Тема: Derived queries (вывод запроса из имени метода)
- Difficulty: 3
- Что нужно сделать: Добавьте в `ProductRepository` метод derived query, возвращающий не более 5 товаров с самыми высокими ценами (сортировка по цене по убыванию). Тест создан заранее (TASK03Test): он проверяет результат на 7 товарах с разными ценами.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // метод должен иметь точно такую сигнатуру:
    List<Product> findTop5ByOrderByPriceDesc();
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t01.TASK03Test#findTop5ByOrderByPriceDescReturnsFiveMostExpensive
```
- Критерий приёмки: Тест: при 7 товарах возвращается ровно 5; цены в порядке убывания; это именно 5 самых дорогих товаров. В SQL-логе виден лимит `fetch first 5 rows only`.
- Подсказка (не решение): ограничение по количеству и сортировка — части имени метода, предикат не обязателен.
