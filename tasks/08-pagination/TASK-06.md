# Задание 06. Page vs Slice: цена в SQL
- Тема: Пагинация (Page / Slice / Pageable / Sort)
- Difficulty: 6
- Что нужно сделать: Тест создан заранее (TASK06Test): на 4 товарах одна и та же страница запрашивается двумя способами — `Page` (через `findAll(Pageable)`) и `Slice` (через derived-метод `findAllByOrderByPriceAsc(Pageable)`, уже существующий в репозитории). Посчитаны prepared statements для каждого способа и зафиксирована разница в цене. Методы в репозиторий добавлять не нужно.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t08.TASK06Test
// Ключевые замеры (Statistics):
//   stats.clear(); Page  page  = productRepository.findAll(PageRequest.of(0, 2));            // 2 SQL
//   stats.clear(); Slice slice = productRepository.findAllByOrderByPriceAsc(PageRequest.of(0, 2)); // 1 SQL
//   контент страниц одинаковый; slice.hasNext() корректен
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t08.TASK06Test#pageCostsTwoQueriesSliceCostsOne
```
- Критерий приёмки: Тест: `Page` — 2 prepared statement (count + данные), `Slice` — 1; содержимое страниц совпадает; у `Slice` нет `getTotalElements`-метаданных (в комментарии зафиксировано: Slice знает только `hasNext`); в SQL-логе slice-запроса: `fetch first ? rows only` без count-вызова; в комментарии: когда `Slice` выгоден (длинные листинги, где total не нужен).
- Подсказка (не решение): `Slice` — «есть ещё страница?» вместо «сколько всего?», и за это не платится count-запрос.
