# Задание 01. Первый native-запрос
- Тема: Native queries
- Difficulty: 1
- Что нужно сделать: Добавьте в `ProductRepository` первый native-запрос: все товары дороже заданной цены, `SELECT *` из таблицы `products`, возврат `List<Product>`. Тест на 3 товарах создан заранее (TASK01Test).
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с native-запросом (nativeQuery = true, см. «Что нужно сделать»)
    // метод должен иметь точно такое имя и @Param:
    List<Product> findNativePricedAbove(@Param("minPrice") BigDecimal minPrice);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t03.TASK01Test#firstNativeQueryReturnsEntities
```
- Критерий приёмки: Тест: с параметром 100 возвращаются 2 товара, поля сущностей заполнены (включая `stockQuantity`); в SQL-логе виден «сырой» SQL против таблицы `products` (не JPQL со сущностями); параметр передаётся prepared statement-ом (`price > ?`).
- Подсказка (не решение): `nativeQuery = true` + имя реальной таблицы и реальные имена колонок из DDL.
