# Задание 04. Несколько SET-полей и выражение в правой части
- Тема: @Modifying
- Difficulty: 4
- Что нужно сделать: Добавить в `ProductRepository` метод с точной сигнатурой из «Стартового кода»: bulk UPDATE (`@Modifying` + `@Query`), который в ОДНОМ запросе меняет два поля — `price := price + :delta` и `stockQuantity := stockQuantity + 1` — только у товаров с ценой < :maxPrice. Метод возвращает число обновлённых строк. Тест уже создан (`TASK04Test`): три товара без категории с ценами 10, 20, 50 и stock 1.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте метод с этой сигнатурой:
    // @Modifying + @Query, в одном UPDATE два SET-поля,
    // в правой части — выражение с текущим значением поля
    int raisePricesAndRestock(@Param("delta") BigDecimal delta, @Param("maxPrice") BigDecimal maxPrice);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t09.TASK04Test#multipleSetClauses
```
- Критерий приёмки: Тест `multipleSetClauses` зелёный: метод возвращает 2; в БД (native SELECT по цене) (15.00, stock 2), (25.00, stock 2), (50.00, stock 1 — не тронут); в SQL-логе один `update products` с двумя изменяемыми столбцами.
- Подсказка (не решение): Несколько полей в SET разделяются запятой; в правой части SET допустимы выражения, использующие текущие поля сущности (`p.price + :delta`).
