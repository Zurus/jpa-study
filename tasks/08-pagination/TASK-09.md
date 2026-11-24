# Задание 09. Пагинация + LAZY: N+1 на странице и лечение @EntityGraph
- Тема: Пагинация (Page / Slice / Pageable / Sort)
- Difficulty: 9
- Что нужно сделать: Добавьте в `ProductRepository` метод с `@EntityGraph(attributePaths = "category")` и `Pageable`, возвращающий `Page<Product>` (запрос в `@Query`, имя метода фиксировано). Тест создан заранее (TASK09Test): 6 товаров / 6 категорий. (а) Страница `findAll(PageRequest.of(0, 2))` + чтение категорий элементов — замер SQL (count + данные + N по одному на каждый товар страницы). (б) Страница вашего метода + тот же цикл — замер после «лечения».
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // метод должен иметь точно такую сигнатуру:
    // TODO: добавьте @Query + @EntityGraph (см. «Что нужно сделать»)
    Page<Product> findAllWithCategoryGraph(Pageable pageable);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t08.TASK09Test#entityGraphCuresNPlus1OnPage
```
- Критерий приёмки: Тест: фаза (а) — 4 prepared statement (count, данные, 2 по категориям); фаза (б) — 2 (count + одна data-SELECT с fetch join, в SQL-логе `join categories` внутри data-запроса, доп. SELECT к категориям нет); `getTotalElements()` в обеих фазах совпадает; все категории на странице инициализированы в фазе (б) (`Hibernate.isInitialized`); в комментарии: `@EntityGraph` + `Pageable` — стандартное лекарство от N+1 в пагинированных списках.
- Подсказка (не решение): fetch-стратегия и пагинация совместимы: граф добавляет join в data-запрос, count остаётся отдельным.
