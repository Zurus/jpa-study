# Задание 05. Проекция в Object[]
- Тема: JPQL @Query
- Difficulty: 5
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`, возвращающий `List<Object[]>`: только id, name, price товаров дороже заданной цены, в заданном порядке. Тест, разбирающий каждую строку по индексам, создан заранее (TASK05Test).
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (см. «Что нужно сделать»)
    // метод должен иметь точно такую сигнатуру:
    List<Object[]> idNamePriceAbove(@Param("minPrice") BigDecimal minPrice);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t02.TASK05Test#objectArrayProjectionHasThreeColumnsInSelectOrder
```
- Критерий приёмки: Тест: размер строки 3; индексы соответствуют порядку SELECT (Long, String, BigDecimal); значений в строках ровно столько, сколько товаров (нет дублей); в SQL-логе выбраны только три колонки `id, name, price` — не `p1_0.*`.
- Подсказка (не решение): тип возвращаемого списка совпадает с типом, который вы «просите» у Spring Data — `List<Object[]>`.
