# Задание 09. Нюанс: bulk @Modifying не участвует в оптимистичной блокировке
- Тема: Нюансы (@Version, оптимистичная блокировка)
- Difficulty: 9
- Что нужно сделать: Добавьте в `ProductRepository` метод `int setStockForAll(@Param("stock") int stock)` — `@Modifying` bulk UPDATE «stock = :stock у ВСЕХ товаров». Заранее созданный тест (TASK09Test) в реальных транзакциях показывает: (1) после bulk UPDATE stock изменился, но `version` остался 0 — `@Modifying` SQL не знает о `@Version`; (2) поэтому "устаревшая" копия, загруженная до bulk-обновления (`version = 0`), успешно сохраняется через `productRepository.save` — `UPDATE ... WHERE version = 0` находит строку, конфликта нет; (3) именно это JPA-обновление поднимает version 0 → 1, пишет ВСЮ строку из копии и перетирает bulk-stock.
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Modifying + @Query (JPQL) — UPDATE products SET stock = :stock
int setStockForAll(@Param("stock") int stock);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t12.TASK09Test#bulkUpdateBypassesVersion
```
- Критерий приёмки: Тест: товар сохранён (цена 100.00, stock 4, version 0); `setStockForAll(7)` возвращает 1, в БД stock = 7 и version = 0 (!); сохранение stale-копии с `setPrice(120.00)` не бросает исключений; финал — цена 120.00, stock = 4 (полная строка из устаревшей копии перетёрла bulk-stock 7), version = 1 (поднят именно JPA-UPDATE).
- Подсказка (не решение): это "тёмная" сторона bulk-обновлений: защита `@Version` распространяется только на SQL, который генерирует сам Hibernate.
