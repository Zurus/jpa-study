# Задание 06. clearAutomatically: PC очищается после запроса
- Тема: @Modifying
- Difficulty: 6
- Что нужно сделать: Добавить в `ProductRepository` метод с точной сигнатурой из «Стартового кода»: bulk UPDATE цен всех товаров категории (по имени). Требование: после выполнения запроса persistence context обязан быть очищен, чтобы НЕМЕДЛЕННЫЙ повторный поиск (без `em.clear()` в тесте) вернул новые значения. Определите, какой флаг `@Modifying` это обеспечивает, и используйте его. Тест уже создан (`TASK06Test`): два товара категории A (цены 10.00 / 20.00) сначала загружаются в PC, затем вызывается метод с новой ценой 99.99.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте метод с этой сигнатурой:
    // @Modifying + @Query, флаг, очищающий PC после bulk-запроса
    int resetPricesInCategory(@Param("categoryName") String categoryName, @Param("price") BigDecimal price);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t09.TASK06Test#clearAutomaticallyMakesFindFresh
```
- Критерии приёмки: Тест `clearAutomaticallyMakesFindFresh` зелёный: метод возвращает 2; немедленный `findById` обоих товаров (без `em.clear()` в тесте) возвращает 99.99; в комментарии к методу записано, что происходит без этого флага (в PC остаются «старые» managed-сущности, и find сразу после bulk UPDATE вернёт прежние значения).
- Подсказка (не решение): У `@Modifying` два флага: `flushAutomatically` и `clearAutomatically` — перечитайте, что делает каждый, и подберите нужный.
