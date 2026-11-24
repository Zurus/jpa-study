# Задание 08. NULL в derived queries
- Тема: Derived queries (вывод запроса из имени метода)
- Difficulty: 8
- Что нужно сделать: Добавьте в `ProductRepository` два метода derived query: товары БЕЗ категории и товары С категорией. Тест создан заранее (TASK08Test): в нём 3 товара (2 с категорией, 1 без) и отдельная проверка-ловушка: вызов derived query с предикатом по `category` и параметром `null`.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // методы должны иметь точно такие сигнатуры:
    List<Product> findByCategoryIsNull();
    List<Product> findByCategoryIsNotNull();
    // ловушка использует уже существующий метод: findByCategory(Category) с аргументом null
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t01.TASK08Test#nullPredicatesAndNullArgumentTrap
```
- Критерий приёмки: Тест: метод «без категории» возвращает ровно 1 товар, «с категорией» — ровно 2; вызов `findByCategory(null)` возвращает ПУСТОЙ список (не ошибку и не товары без категории) — SQL-сравнение `= null` истинно никогда; в SQL-логе для правильных методов видны `is null` / `is not null`.
- Подсказка (не решение): предикаты для null не принимают параметров; передача null в обычный предикат — классическая ловушка.
