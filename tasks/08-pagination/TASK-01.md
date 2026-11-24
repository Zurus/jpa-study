# Задание 01. Первый Page
- Тема: Пагинация (Page / Slice / Pageable / Sort)
- Difficulty: 1
- Что нужно сделать: Тест создан заранее (TASK01Test): 5 товаров, вызов ядрового `productRepository.findAll(PageRequest.of(0, 2))` — проверка содержимого первой страницы и общего количества элементов. Методы в репозиторий добавлять не нужно: `findAll(Pageable)` — часть `JpaRepository`.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t08.TASK01Test
// Ключевые вызовы:
//   Page<Product> page = productRepository.findAll(PageRequest.of(0, 2));
//   page.getContent() -> 2 элемента; page.getTotalElements() -> 5
//   Statistics: на один вызов 2 prepared statement (count + данные)
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t08.TASK01Test#firstPageHasContentAndTotalElements
```
- Критерий приёмки: Тест: `getContent()` — ровно 2 элемента (размер страницы), `getTotalElements() == 5` (все товары, а не только страница); в SQL-логе 2 запроса: count (`select count(...)`) и данные (`fetch first ? rows only`); в комментарии зафиксировано, почему `Page` стоит два запроса.
- Подсказка (не решение): `Page` = «данные страницы» + «метаданные обо всём множестве» — метаданные требуют count.
