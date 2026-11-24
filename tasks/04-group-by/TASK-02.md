# Задание 02. Сортировка по агрегату
- Тема: GROUP BY / HAVING
- Difficulty: 2
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`: группы товаров по имени с сортировкой по РАЗМЕРУ группы по убыванию, при равных размерах — по алфавиту. Возврат `Object[]` [название, количество]. Тест с 3 группами разного размера создан заранее (TASK02Test).
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (GROUP BY / агрегаты, см. «Что нужно сделать»)
    // метод должен иметь точно такое имя (без параметров):
    List<Object[]> countByNameByGroupSizeDesc();
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t04.TASK02Test#orderGroupsBySizeDescThenName
```
- Критерий приёмки: Тест: порядок групп — [Bbb(3), Aaa(1), Ccc(1)]: самая большая группа первой, «Aaa» и «Ccc» (равные размеры) по алфавиту; в SQL-логе `order by count(...) desc, name asc` (сортировка именно по выражению агрегата); в комментарии зафиксировано: сортировка по агрегату невозможна без GROUP BY.
- Подсказка (не решение): в ORDER BY сгруппированного запроса допустимы агрегаты и ключи групп, но не «обычные» колонки, не вошедшие в GROUP BY.
