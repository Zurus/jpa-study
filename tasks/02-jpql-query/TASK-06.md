# Задание 06. Выборка одного поля: List<String>
- Тема: JPQL @Query
- Difficulty: 6
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`, возвращающий `List<String>` — имена товаров, у которых цена строго между двумя переданными значениями (оба конца не включены), отсортированные по имени по возрастанию. Имена параметров в запросе — `:from` и `:to`. Тест создан заранее (TASK06Test): 4 товара, проверяются границы диапазона и порядок.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (см. «Что нужно сделать»)
    // метод должен иметь точно такую сигнатуру:
    List<String> findNamesByPriceBetween(@Param("from") BigDecimal from, @Param("to") BigDecimal to);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t02.TASK06Test#singleFieldProjectionReturnsOnlyNames
```
- Критерий приёмки: Тест: вызов с (100, 200) возвращает ровно 1 имя — товар с ценой 150 (границы 100 и 200 исключены); вызов с (99, 251) возвращает все 4 имени в алфавитном порядке (сортировка по имени, а не по цене); возвращаемый тип — `List<String>` (не сущности, не `Object[]`); в SQL-логе в SELECT-части только колонка `name`.
- Подсказка (не решение): в SELECT-части JPQL можно указать не сущность целиком, а отдельное её свойство — тогда Hibernate выберет только одну колонку.
