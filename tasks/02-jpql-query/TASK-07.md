# Задание 07. JPQL + пагинация Pageable
- Тема: JPQL @Query
- Difficulty: 7
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`, возвращающий `Page<Product>`: товары с ценой в диапазоне [от, до], последний параметр — `Pageable`. Тест создан заранее (TASK07Test): 5 товаров, `pageSize = 2`, обход страниц 0..2.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (см. «Что нужно сделать»)
    // метод должен иметь точно такую сигнатуру:
    Page<Product> findPageByPriceBetween(@Param("from") BigDecimal from,
                                         @Param("to") BigDecimal to,
                                         Pageable pageable);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t02.TASK07Test#jpqlPageByPriceBetween
```
- Критерий приёмки: Тест: страница 0 — 2 элемента, страница 1 — 2, страница 2 — 1; `getTotalElements() == 5`, `getTotalPages() == 3`, на последней странице `hasNext == false`; в SQL-логе данные-запрос содержит `fetch first ? rows only` (и offset-часть со 2-й страницы), а отдельно выполняется count-запрос.
- Подсказка (не решение): `Pageable` как последний параметр — Spring Data сам добавит лимит и смещение к вашему JPQL.
