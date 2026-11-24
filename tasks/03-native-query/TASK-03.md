# Задание 03. Проекция в Object[]
- Тема: Native queries
- Difficulty: 3
- Что нужно сделать: Добавьте в `ProductRepository` native-запрос, возвращающий `List<Object[]>` с колонками id, name, price (порядок именно такой) для товаров дороже заданной цены, отсортированных по имени. Тест, вручную маппящий строки в record `ProductNamePrice`, создан заранее (TASK03Test). Метод с таким @Query УЖЕ ЕСТЬ в ProductRepository — используйте его имя `nativeSearchByPriceAbove`.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с native-запросом (nativeQuery = true, см. «Что нужно сделать»)
    // метод с таким @Query уже существует в репозитории, имя — nativeSearchByPriceAbove:
    List<Object[]> nativeSearchByPriceAbove(@Param("minPrice") BigDecimal minPrice);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t03.TASK03Test#objectArrayRowsMapManuallyToRecord
```
- Критерий приёмки: Тест: каждая строка — массив из 3 элементов (Long, String, BigDecimal); ручной маппинг в record даёт ожидаемые пары; порядок строк — по имени; в SQL-логе выбраны ровно три колонки.
- Подсказка (не решение): `Object[]` — универсальный контейнер для проекции, порядок элементов = порядок колонок в SELECT.
