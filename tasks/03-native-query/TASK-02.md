# Задание 02. Явный список колонок и ORDER BY (snake_case)
- Тема: Native queries
- Difficulty: 2
- Что нужно сделать: Добавьте в `ProductRepository` native-запрос с ЯВНЫМ списком колонок (без `*`): id, name, price, stock — и детерминированной сортировкой по имени с тай-брейком по id. Тест создан заранее (TASK02Test): он доказывает, что имя свойства `stockQuantity` в этом SQL-контексте не существует.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с native-запросом (nativeQuery = true, см. «Что нужно сделать»)
    // метод должен иметь точно такую сигнатуру:
    List<Product> findNativePricedAtLeastWithColumns(@Param("min") BigDecimal min);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t03.TASK02Test#explicitColumnListWithSnakeCaseAndIdTieBreaker
```
- Критерий приёмки: Тест: два товара с одинаковым именем возвращаются в порядке возрастающего id; все поля сущности заполнены; в SQL-логе используется DDL-имя колонки (snake_case); отдельная часть теста (динамический `em.createNativeQuery` с `stockQuantity`) фиксирует ошибку БД/парсера — `unknown column`.
- Подсказка (не решение): в native SQL живёт только то, что описано в DDL: `stock_quantity`, `category_id`.
