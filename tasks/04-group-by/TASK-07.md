# Задание 07. Constructor expression по группам
- Тема: GROUP BY / HAVING
- Difficulty: 7
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`, возвращающий `List<ProductNamePrice>` (record из `com.example.jpalab.dto`): по каждой категории — название и СУММА цен (constructor expression поверх групп с LEFT JOIN). Тест создан заранее (TASK07Test): категория с товарами, пустая категория.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (GROUP BY / агрегаты, см. «Что нужно сделать»)
    // метод должен иметь точно такое имя (без параметров):
    List<ProductNamePrice> categoryNameWithSumOfPrices();
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t04.TASK07Test#constructorExpressionOverGroups
```
- Критерий приёмки: Тест: запись A — сумма 30 (`isEqualByComparingTo`); запись B — сумма NULL (record допускает null) — это ожидание ЗАФИКСИРОВАНО в тесте; тип результата — список record, а не `Object[]`; в SQL-логе `group by` и `sum` в одном запросе.
- Подсказка (не решение): агрегат передаётся в конструктор так же, как обычное поле — но помните, что пустая группа даёт NULL.
