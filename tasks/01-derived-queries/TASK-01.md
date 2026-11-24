# Задание 01. Простой предикат GreaterThan
- Тема: Derived queries (вывод запроса из имени метода)
- Difficulty: 1
- Что нужно сделать: Добавьте в `ProductRepository` метод derived query (без `@Query`), возвращающий все товары, у которых количество на складе строго больше переданного значения. Тест создан заранее (TASK01Test): он проверяет результат на 3 товарах с разным `stockQuantity`.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // метод должен иметь точно такую сигнатуру:
    List<Product> findByStockQuantityGreaterThan(int minStock);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t01.TASK01Test#derivedGreaterThanFiltersByStockQuantity
```
- Критерий приёмки: Тест: для порога 4 возвращается ровно 2 товара (stock 5 и 10), для порога 0 — 2 (stock строго больше 0), для порога 10 — пусто. В SQL-логе виден один SELECT с условием `stock_quantity > ?`.
- Подсказка (не решение): имя = `findBy` + имя свойства + ключевое слово сравнения.
