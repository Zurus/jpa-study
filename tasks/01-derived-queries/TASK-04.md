# Задание 04. Комбинация условий: Containing + Between
- Тема: Derived queries (вывод запроса из имени метода)
- Difficulty: 4
- Что нужно сделать: Добавьте в `ProductRepository` метод derived query: товары, у которых имя содержит переданный фрагмент И цена входит в диапазон [от, до] включительно. Тест создан заранее (TASK04Test): он проверяет оба условия и включаемость границ диапазона.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // метод должен иметь точно такую сигнатуру:
    // (регистронезависимость зафиксирована в имени метода — IgnoreCase)
    List<Product> findByNameContainingIgnoreCaseAndPriceBetween(
            String nameFragment, BigDecimal minPrice, BigDecimal maxPrice);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t01.TASK04Test#nameContainingAndPriceBetweenIncludesBoundaries
```
- Критерий приёмки: Тест: граница диапазона включена — товар с ценой ровно 100 и ровно 200 присутствуют в результате; "Beta 200" отфильтрован по имени (регистр учтён по вашему выбору — зафиксировать в имени метода); в SQL-логе видны и `like`, и `between ? and ?` в одном WHERE.
- Подсказка (не решение): условия, связанные AND, просто конкатенируются в имени через And.
