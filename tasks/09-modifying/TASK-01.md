# Задание 01. Первый bulk UPDATE
- Тема: @Modifying
- Difficulty: 1
- Что нужно сделать: Добавить в `ProductRepository` метод с точной сигнатурой из «Стартового кода»: bulk UPDATE в JPQL (`@Modifying` + `@Query`), который устанавливает цену всем товарам категории (категория задаётся по имени) на заданное значение. Метод возвращает число обновлённых строк. Тест уже создан (`TASK01Test`): в категории A два товара (P1, P2), в категории B один (P3).
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте метод с этой сигнатурой:
    // @Modifying + @Query с bulk UPDATE в JPQL (см. «Что нужно сделать»)
    int updatePricesInCategory(@Param("categoryName") String categoryName, @Param("price") BigDecimal price);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t09.TASK01Test#firstBulkUpdate
```
- Критерии приёмки: Тест `firstBulkUpdate` зелёный: метод возвращает 2; в БД (native SELECT) у обоих товаров категории A цена 99.99, а у товара категории B цена не изменилась (30.00); в SQL-логе один вызов `update products set price = ? where ...` (один bulk UPDATE, а не per-entity UPDATE).
- Подсказка (не решение): Bulk UPDATE в JPQL имеет вид `UPDATE Сущность e SET e.поле = :значение WHERE <условие>`; метод с `@Modifying` возвращает `int` — количество затронутых строк.
