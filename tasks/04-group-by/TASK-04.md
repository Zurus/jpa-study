# Задание 04. Несколько агрегатов в одной строке
- Тема: GROUP BY / HAVING
- Difficulty: 4
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`: по каждой категории одна строка `Object[]` [название категории, количество товаров, сумма цен, минимальная цена, максимальная цена]. Тест с двумя категориями и проверкой ВСЕХ пяти значений создан заранее (TASK04Test).
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (GROUP BY / агрегаты, см. «Что нужно сделать»)
    // метод должен иметь точно такое имя (без параметров):
    List<Object[]> categoryAggregates();
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t04.TASK04Test#multipleAggregatesPerGroup
```
- Критерий приёмки: Тест: для категории A строка [A, 2, 30, 10, 20], для B — [B, 1, 5, 5, 5]; типы: Long, Long, BigDecimal, BigDecimal, BigDecimal (сравнение цен — `isEqualByComparingTo`); в SQL-логе один запрос с `count`, `sum`, `min`, `max` одновременно.
- Подсказка (не решение): в SELECT сгруппированного запроса — ключ группы и любое число агрегатов.
