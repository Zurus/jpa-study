# Задание 08. NULL и COUNT: пустая группа
- Тема: GROUP BY / HAVING
- Difficulty: 8
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`: для каждой категории (LEFT JOIN к товарам) строка `Object[]` [название, COUNT(*), COUNT(p.id), COUNT(c.name), SUM(p.price)]. Тест с категорией, в которой 0 товаров, создан заранее (TASK08Test); он проверяет значения всех четырёх агрегатов именно для ПУСТОЙ группы.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (GROUP BY / агрегаты, см. «Что нужно сделать»)
    // метод должен иметь точно такое имя (без параметров):
    List<Object[]> nullAggregateRows();
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t04.TASK08Test#nullAggregatesInEmptyGroup
```
- Критерий приёмки: Тест для пустой группы "Empty": `COUNT(*) = 1` (строка существует), `COUNT(p.id) = 0` (нет ненулевых значений id товара), `COUNT(c.name) = 1`, `SUM(p.price) = null`; для "Full": 1, 1, 1, 7.50; в комментарии зафиксировано различие: `COUNT(*)` считает строки, `COUNT(col)` — только ненулевые значения колонки, `SUM` без строк — NULL (а не 0).
- Подсказка (не решение): агрегаты по колонкам «нулевой» стороны LEFT JOIN дают 0/null, а по колонкам «справа» — 1.
