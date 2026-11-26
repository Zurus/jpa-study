# Задание 08. Сервис saveWithRetry: retry после конфликта @Version
- Тема: Нюансы (@Version, оптимистичная блокировка)
- Difficulty: 8
- Что нужно сделать: В том же `ProductVersioningService` реализуйте метод `Object[] saveWithRetry(Product product, BigDecimal targetPrice, int maxAttempts)` — классический retry-паттерн при оптимистичной блокировке. ПРАВИЛА (пинованы тестом): (1) ПЕРВАЯ попытка сохраняет переданный экземпляр БЕЗ предварительной перезагрузки; (2) при `OptimisticLockingFailureException` — новая `REQUIRES_NEW`-транзакция, `em.find(product.getId())`, `setPrice(targetPrice)` и повтор; (3) `attempts` — число реально выполненных `productRepository.save`. Возврат: `[attempts (Integer), итоговый version (Long), итоговая цена (BigDecimal)]`. Тест сам создаёт конфликт (version 0 → 1 до вызова) и передаёт устаревшую копию: ожидается ровно 2 попытки и финальный version 2.
- Стартовый код:
```java
// TODO: добавьте в ProductVersioningService:
public Object[] saveWithRetry(Product product, BigDecimal targetPrice, int maxAttempts) {
    // TODO: цикл попыток; первая — переданный экземпляр;
    //       после OptimisticLockingFailureException — em.find + setPrice(targetPrice);
    //       attempts — счётчик реально выполненных save
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t12.TASK08Test#saveWithRetrySucceedsOnSecondAttempt
```
- Критерий приёмки: Тест: результат `[2, 2, 300.00]`; независимая native-проверка: в БД цена 300.00, version = 2.
- Подсказка (не решение): после перезагрузки сущность получает актуальный `version` — её `save` проходит; не забывайте, что `targetPrice` нужно ставить и на пере-загруженную копию.
