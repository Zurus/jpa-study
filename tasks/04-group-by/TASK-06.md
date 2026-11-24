# Задание 06. LEFT JOIN: пустые группы
- Тема: GROUP BY / HAVING
- Difficulty: 6
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`: количество товаров в КАЖДОЙ категории, включая пустые (LEFT JOIN от категории к коллекции товаров), возврат `Object[]` [название, количество]. Тест с категорией, в которой 0 товаров, создан заранее (TASK06Test); он также доказывает, что INNER JOIN пустую группу «съел» бы.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (GROUP BY / агрегаты, см. «Что нужно сделать»)
    // метод должен иметь точно такое имя (без параметров):
    List<Object[]> productCountPerCategory();
}

// В тесте есть и дополнительный динамический запрос с обычным JOIN
// (тот же SELECT, но JOIN c.products p вместо LEFT JOIN)```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t04.TASK06Test#leftJoinKeepsEmptyGroups
```
- Критерий приёмки: Тест: с LEFT JOIN строки ["Empty", 0] и ["Full", 2]; дополнительный запрос с обычным `JOIN` возвращает только ["Full", 2] — пустая группа исчезает (зафиксировать в тесте); в SQL-логе `left outer join`.
- Подсказка (не решение): идите от «родительной» сущности и LEFT JOIN-ьте коллекцию — пустые группы сохранятся.
