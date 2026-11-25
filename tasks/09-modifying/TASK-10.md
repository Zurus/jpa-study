# Задание 10. Оба флага в реальной транзакции
- Тема: @Modifying
- Difficulty: 10
- Что нужно сделать: Добавить в `ProductRepository` метод с точной сигнатурой из «Стартового кода»: bulk UPDATE «stock := stock − :penalty» для товаров категории (по имени), у которых `stock >= :penalty`. Обязательны ОБА флага: `flushAutomatically = true` (pending-изменения PC видны bulk-запросу) и `clearAutomatically = true` (PC очищается после запроса). Тест уже создан (`TASK10Test`, `@SpringBootTest` + `TransactionTemplate`): перед вызовом stock P1 «грязный» в PC (8, не flush-нут; в БД 10), P2 = 3; penalty = 5.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте метод с этой сигнатурой:
    // @Modifying(clearAutomatically = true, flushAutomatically = true) + @Query,
    // bulk UPDATE с условием stock >= :penalty
    int applyStockPenaltyInCategory(@Param("categoryName") String categoryName, @Param("penalty") int penalty);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t09.TASK10Test#bulkUpdateWithBothFlagsInRealTransaction
```
- Критерии приёмки: Тест `bulkUpdateWithBothFlagsInRealTransaction` зелёный: метод возвращает 1 (P1: 8 − 5 = 3, т.к. flushAutomatically сделал «грязные» 8 видимыми; P2: 3 < 5 — не участвует); немедленный `findById` P1 без `em.clear()` возвращает 3 (clearAutomatically очистил PC, а не вернул старую сущность с 8); в закоммиченном состоянии (новая транзакция) P1 = 3 и P2 = 3; в комментарии записано, что вернёт тест при убирании каждого из флагов.
- Подсказка (не решение): `flushAutomatically` отвечает на вопрос «что запрос УВИДИТ», `clearAutomatically` — «что будет в PC ПОСЛЕ»; без первого bulk UPDATE посчитает по старым данным из БД, без второго find вернёт «старое» из PC.
