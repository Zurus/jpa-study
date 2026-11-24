# Задание 05. HAVING: фильтр по группам
- Тема: GROUP BY / HAVING
- Difficulty: 5
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`: категории, в которых товаров НЕ МЕНЬШЕ переданного количества (параметр `:minProducts`), с количеством — `Object[]` [название, количество]. Тест, доказывающий разницу WHERE и HAVING, создан заранее (TASK05Test).
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (GROUP BY / агрегаты, см. «Что нужно сделать»)
    // метод должен иметь точно такую сигнатуру с @Param:
    List<Object[]> categoryCountsAtLeast(@Param("minProducts") int minProducts);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t04.TASK05Test#havingFiltersGroups
```
- Критерий приёмки: Тест: с `minProducts = 2` — только ["Many", 3]; с `minProducts = 1` — все три группы; в SQL-логе условие над агрегатом находится в `having`, а не в `where`; в комментарии к тесту зафиксировано, что `WHERE COUNT(...) >= 2` — ошибка (WHERE предшествует группировке), и продемонстрировано, что HAVING — единственный способ отфильтровать группы.
- Подсказка (не решение): WHERE фильтрует строки ДО группировки, HAVING — группы ПОСЛЕ агрегации.
