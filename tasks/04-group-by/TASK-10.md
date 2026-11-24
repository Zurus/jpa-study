# Задание 10. Page по сгруппированному запросу
- Тема: GROUP BY / HAVING
- Difficulty: 10
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`, возвращающий `Page<Object[]>`: категории с количеством товаров (GROUP BY + LEFT JOIN), последний параметр — `Pageable`. Тест создан заранее (TASK10Test): 3 категории (2 товара, 0 товаров, 0 товаров), `pageSize = 2`; он проверяет, ЧТО именно считает Spring Data в `totalElements`.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (GROUP BY / агрегаты, см. «Что нужно сделать»)
    // метод должен иметь точно такое имя + Pageable:
    Page<Object[]> categoryCountPage(Pageable pageable);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t04.TASK10Test#pageOverGroups
```
- Критерий приёмки: Тест: `getTotalElements() == 3` — это число ГРУПП (категорий), а не число строк-товаров (их всего 2, обе в категории A); страница 0 — 2 группы, страница 1 — 1 группа, `hasNext == false`; в SQL-логе count-запрос обёрнут в подзапрос вокруг сгруппированного SELECT (`select count(*) from (...)`), а не «обнулён» до простого count по таблицам — зафиксировать цитатой из лога.
- Подсказка (не решение): Spring Data для GROUP BY-запросов строит count как подзапрос вокруг всего сгруппированного SELECT — total = количество групп.
