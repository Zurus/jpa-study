# Задание 05. JOIN FETCH с WHERE и ORDER BY
- Тема: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
- Difficulty: 5
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`: товары, чья КАТЕГОРИЯ имеет имя, равное переданному, через `JOIN FETCH p.category c` + условие `WHERE c.name = :categoryName` + `ORDER BY p.price DESC`. Тест создан заранее (TASK05Test): отфильтровано ровно товары нужной категории, порядок по цене desc, запрос — один.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JOIN FETCH + WHERE + ORDER BY (см. «Что нужно сделать»)
    // метод должен иметь точно такую сигнатуру:
    List<Product> findWithCategoryByNameFetched(@Param("categoryName") String categoryName);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t07.TASK05Test#fetchJoinWithFilterAndOrderByCategoryName
```
- Критерий приёмки: Тест: результат содержит только товары категории A, отсортированы по цене desc (30 перед 10); ровно 1 prepared statement (фильтр и сортировка не порождают доп. запросов); `getCategory()` у результатов инициализирован; в SQL-логе `where` по имени категории и `order by` внутри той же SELECT с fetch join.
- Подсказка (не решение): условие и сортировка пишутся поверх алиаса join-а — fetch join остаётся одним запросом.
