# Задание 04. Лечение @EntityGraph
- Тема: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
- Difficulty: 4
- Что нужно сделать: Добавьте в `ProductRepository` derived-метод с `@EntityGraph(attributePaths = "category")` и предикатом «товары дороже цены». Тест создан заранее (TASK04Test): доказано, что fetch join добавлен автоматически (одна SELECT с join), предикат работает, и N+1 отсутствует.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @EntityGraph к derived-методу (см. «Что нужно сделать»)
    // метод должен иметь точно такую сигнатуру:
    List<Product> findAllByPriceGreaterThan(BigDecimal minPrice);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t07.TASK04Test#entityGraphAddsFetchJoinToDerivedQuery
```
- Критерий приёмки: Тест: ровно 1 prepared statement; в SQL-логе fetch join к `categories` и условие по цене в одном запросе; предикат отфильтровал ожидаемые товары (проверка содержимого); все категории в результате инициализированы, доп. SELECT-ов нет; в комментарии: `@EntityGraph` — декларативный fetch join поверх derived/явного запроса.
- Подсказка (не решение): `@EntityGraph` не требует писать JOIN в JPQL — Hibernate достроит fetch join сам.
