# Задание 01. Первый JPQL-запрос с именованным параметром
- Тема: JPQL @Query
- Difficulty: 1
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query` с JPQL: все товары дороже переданной цены. Имя параметра в запросе — `:minPrice`, сигнатура — один параметр `BigDecimal`. Тест на 3 товарах с двумя разными значениями параметра создан заранее (TASK01Test).
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (см. «Что нужно сделать»)
    // метод должен иметь точно такую сигнатуру:
    List<Product> findPricedAbove(@Param("minPrice") BigDecimal minPrice);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t02.TASK01Test#jpqlNamedQueryFiltersByPrice
```
- Критерий приёмки: Тест: вызов с 100 возвращает 2 товара, с 200 — 1 (граница не включена); параметр передаётся как именованный (`@Param`). В SQL-логе запрос идёт prepared statement-ом: `where price > ?` (значение не встроено в текст).
- Подсказка (не решение): `:имя` в запросе связывается с аргументом через аннотацию на параметре метода.
