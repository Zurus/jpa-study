# Задание 06. Page vs Slice: цена в SQL
- Тема: Пагинация (Page / Slice / Pageable / Sort)
- Difficulty: 6
- Что нужно сделать: Два метода в `ProductRepository`: (1) `Page<Product> findPricedAtLeast(@Param("minPrice") BigDecimal minPrice, Pageable pageable)` — см. задание 01; (2) `Slice<Product> findPricedSlice(@Param("minPrice") BigDecimal minPrice, Pageable pageable)` — та же логика (цена >= `:minPrice`), но возвращает `Slice`. Заранее созданный тест (TASK06Test) запрашивает ОДНУ И ТУ ЖЕ страницу двумя способами, считает prepared statements и фиксирует разницу в цене.
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: два метода с одной логикой (p.price >= :minPrice):
Page<Product> findPricedAtLeast(@Param("minPrice") BigDecimal minPrice, Pageable pageable);
Slice<Product> findPricedSlice(@Param("minPrice") BigDecimal minPrice, Pageable pageable);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t08.TASK06Test#pageCostsTwoQueriesSliceCostsOne
```
- Критерий приёмки: Тест: `Page` (findPricedAtLeast) — 2 prepared statement (count + данные), `Slice` (findPricedSlice) — 1; содержимое страниц совпадает (имена P2, P3); у `Slice` нет `getTotalElements`-метаданных (в комментарии зафиксировано: Slice знает только `hasNext`); `hasNext() == true`, `hasPrevious() == false`; в комментарии: когда `Slice` выгоден (длинные листинги, где total не нужен).
- Подсказка (не решение): `Slice` — «есть ещё страница?» вместо «сколько всего?», и за это не платится count-запрос.
