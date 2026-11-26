# Задание 07. flushAutomatically: запрос видит незаflushенные изменения
- Тема: @Modifying
- Difficulty: 7
- Что нужно сделать: Добавьте в `ProductRepository` метод `int setStockForPricedAtLeast(@Param("stock") int stock, @Param("minPrice") BigDecimal minPrice)` — `@Modifying(flushAutomatically = true)` bulk UPDATE «stock = :stock у товаров с ценой >= :minPrice» (граница включена). Заранее созданный тест (TASK07Test): цена товара A изменена в persistence context (100 → 300), но изменение НЕ flush-нуто; затем выполняется bulk UPDATE «stock = 0, где цена >= 250» — флаг `flushAutomatically` должен выгнать pending-изменение в БД ПЕРЕД bulk-запросом, чтобы WHERE увидел новую цену.
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Modifying(flushAutomatically = true) + @Query —
//       UPDATE products SET stock = :stock WHERE price >= :minPrice
int setStockForPricedAtLeast(@Param("stock") int stock,
                             @Param("minPrice") BigDecimal minPrice);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t09.TASK07Test#flushAutomaticallySeesUnflushedChanges
```
- Критерий приёмки: Тест: данные — A (100.00, stock 5) и B (200.00, stock 6); A «грязный» в PC (цена 300.00, без flush); вызов `setStockForPricedAtLeast(0, 250.00)` возвращает 1 (а не 0!) — bulk-запрос увидел незаflushенную цену 300; в БД (native SELECT) у A stock 0, у B stock 6 (200 < 250 — не тронут); в SQL-логе ПЕРЕД bulk UPDATE виден `update products` для pending-изменения товара A (именно это делает flushAutomatically).
- Подсказка (не решение): Bulk-запрос, как и любой запрос, выполняется против БД: если изменения из PC не выгружены, WHERE-условие видит старые данные; `flushAutomatically` — автоматический `em.flush()` перед выполнением.
