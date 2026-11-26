# Задание 01. Первый Page
- Тема: Пагинация (Page / Slice / Pageable / Sort)
- Difficulty: 1
- Что нужно сделать: Добавьте в `ProductRepository` метод `Page<Product> findPricedAtLeast(@Param("minPrice") BigDecimal minPrice, Pageable pageable)` — товары с ценой НЕ МЕНЬШЕ `:minPrice` (граница включена) страницей. Заранее созданный тест (TASK01Test) вызывает его с `PageRequest.of(0, 2)` на 5 товарах (цены 10…50) и проверяет содержимое первой страницы, общее количество элементов и цену запроса (count + данные = 2 SQL).
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Query (JPQL) — p.price >= :minPrice, возвращает Page<Product>
Page<Product> findPricedAtLeast(@Param("minPrice") BigDecimal minPrice, Pageable pageable);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t08.TASK01Test#firstPageHasContentAndTotalElements
```
- Критерий приёмки: Тест: 5 товаров (10, 20, 30, 40, 50); вызов с `minPrice = 20.00` и `PageRequest.of(0, 2)` — ровно 2 prepared statement (count + данные), `getContent()` — 2 элемента (20.00 и 30.00), `getTotalElements() == 4` (все подходящие, а не только страница); в комментарии зафиксировано, почему `Page` стоит два запроса.
- Подсказка (не решение): `Page` = «данные страницы» + «метаданные обо всём множестве» — метаданные требуют count; `Pageable` в аргументе метода сам подставляет лимит/офсет/сортировку.
