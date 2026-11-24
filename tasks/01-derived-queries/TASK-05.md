# Задание 05. Свойство-путь через ассоциацию + First
- Тема: Derived queries (вывод запроса из имени метода)
- Difficulty: 5
- Что нужно сделать: Добавьте в `ProductRepository` метод derived query, возвращающий первый товар (любой, но один) по имени категории — метод принимает строку `categoryName`, а не объект `Category`. Тест создан заранее (TASK05Test): в нём категория с 2 товарами.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // метод должен иметь точно такую сигнатуру:
    Optional<Product> findFirstByCategoryName(String categoryName);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t01.TASK05Test#findFirstByCategoryNameReturnsSingleProduct
```
- Критерий приёмки: Тест: при 2 товарах в категории возвращается ровно 1; для несуществующего имени категории — пустой Optional/пустой результат без ошибки. В SQL-логе запрос затрагивает обе таблицы (join или подзапрос к categories).
- Подсказка (не решение): точка/слитое написание в имени метода — это проход по свойству-пути через ассоциацию.
