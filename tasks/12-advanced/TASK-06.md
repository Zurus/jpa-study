# Задание 06. Оптимистичная блокировка: OptimisticLockingFailureException через репозиторий
- Тема: Нюансы (@Version, оптимистичная блокировка)
- Difficulty: 6
- Что нужно сделать: Добавьте в `ProductVersioningService` (бина `productVersioningService`, см. задание 03) метод `String staleSaveExceptionType(Long id, BigDecimal winnerPrice, BigDecimal stalePrice)`: (1) в одной транзакции загрузить копию (`em.find`) — "читатель"; (2) в отдельной транзакции обновить цену сущности до `winnerPrice` через `em` — "победитель"; (3) у "проигравшей" копии установить `stalePrice` и сохранить через `productRepository.save(stale)` в своей транзакции; (4) поймать исключение и вернуть ИМЯ его класса. Заранее созданный тест (TASK06Test) проверяет: на границе Spring Data Hibernate'овский `StaleObjectStateException` транслируется в `org.springframework.dao.OptimisticLockingFailureException`, а в БД осталось значение победителя с `version = 1`.
- Стартовый код:
```java
// com/example/jpalab/service/ProductVersioningService.java

// TODO: stale = inTx(find); inTx(setPrice(winnerPrice));
//       stale.setPrice(stalePrice); inTx(() -> repository.save(stale))
//       -> catch -> вернуть имя класса исключения
public String staleSaveExceptionType(Long id, BigDecimal winnerPrice, BigDecimal stalePrice) {
    // TODO
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t12.TASK06Test#staleSaveViaRepositoryThrowsOptimisticLockingFailureException
```
- Критерий приёмки: Тест: товар сохранён (цена 100.00, version 0); вызов `staleSaveExceptionType(id, 200.00, 999.99)` возвращает строку `OptimisticLockingFailureException`; в БД цена победителя (200.00), `version = 1`.
- Подсказка (не решение): транслируют исключение прокси Spring Data (`PersistenceExceptionTranslationPostProcessor`); через `em.merge` прилетел бы JPA `OptimisticLockException` — это и есть нюанс, который проверяет задание.
