# Задание 07. Сервис loseTheRace: "гонка" двух обновлений
- Тема: Нюансы (@Version, оптимистичная блокировка)
- Difficulty: 7
- Что нужно сделать: Создайте класс `com.example.jpalab.service.ProductVersioningService` (`@Service`, бин `productVersioningService`; общий контракт — `tasks/12-advanced/README.md`) и реализуйте в нём метод `Object[] loseTheRace(Long id, BigDecimal winnerPrice, BigDecimal loserPrice)`. Каждый шаг — своя `REQUIRES_NEW`-транзакция: (1) `find(id)` — "читатель" забирает копию; (2) `find(id)`, `setPrice(winnerPrice)`, commit — "победитель"; (3) `stale.setPrice(loserPrice)` и `productRepository.save(stale)` — проигравший должен поймать `OptimisticLockingFailureException` (именно через репозиторий — только там Spring делает трансляцию). Метод возвращает `[итоговый version (Long), итоговая цена (BigDecimal), поймал ли проигравший (Boolean)]` (финальное чтение — native-запросом).
- Стартовый код:
```java
// TODO: создайте src/main/java/com/example/jpalab/service/ProductVersioningService.java
// (@Service; EntityManager + ProductRepository + PlatformTransactionManager;
//  каждый шаг — своя REQUIRES_NEW-транзакция) и добавьте метод:
public Object[] loseTheRace(Long id, BigDecimal winnerPrice, BigDecimal loserPrice) {
    // TODO
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t12.TASK07Test#loseTheRace
```
- Критерий приёмки: Тест: результат `[1, 200.00, true]` (version победителя, его цена, проигравший поймал исключение); независимая native-проверка: в БД цена 200.00, version = 1.
- Подсказка (не решение): устаревшая копия — детэчед (её транзакция закрыта); `save` с непустым id = `merge`; конфликт всплывает на flush/commit внутри `save`.
