# Задание 06. Коннектор Or и параметр Sort
- Тема: Derived queries (вывод запроса из имени метода)
- Difficulty: 6
- Что нужно сделать: Добавьте в `ProductRepository` метод derived query: товары, у которых имя содержит фрагмент ИЛИ цена не больше заданной. Последний параметр метода — `Sort`, передаваемый вызывающим кодом. Тест создан заранее (TASK06Test): он доказывает OR-семантику и влияние Sort на порядок.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // метод должен иметь точно такую сигнатуру:
    List<Product> findByNameContainingOrPriceLessThanEqual(
            String nameFragment, BigDecimal maxPrice, Sort sort);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t01.TASK06Test#orPredicateAndExternalSort
```
- Критерий приёмки: Тест: результат — объединение двух множеств (3 из 4 товаров), а не пересечение; вызовы с `Sort.by("price", ASC)` и `Sort.by("name", DESC)` дают соответствующие порядки; в SQL-логе в WHERE есть `or`.
- Подсказка (не решение): коннектор Or пишется в имени метода, а сортировка не «зашивается» в название — она приходит как параметр `Sort`.
