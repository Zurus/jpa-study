# Задание 02. Sort внутри Pageable
- Тема: Пагинация (Page / Slice / Pageable / Sort)
- Difficulty: 2
- Что нужно сделать: Добавьте в `ProductRepository` derived-метод `Page<Product> findByStockQuantityGreaterThanEqual(int minStock, Pageable pageable)` — товары с остатком НЕ МЕНЬШЕ `minStock` (граница включена) страницей. Заранее созданный тест (TASK02Test) запрашивает одну и ту же страницу с разным `Sort` (по возрастанию / по убыванию остатка) и со смещением на вторую страницу, доказывая: сортировка применяется к СТРАНИЦЕ и задаётся из вызывающего кода, а не в SQL метода.
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: derived query — findByStockQuantityGreaterThanEqual(int, Pageable)
Page<Product> findByStockQuantityGreaterThanEqual(int minStock, Pageable pageable);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t08.TASK02Test#sortIsPassedByCallerNotHardcoded
```
- Критерий приёмки: Тест: 5 товаров со стоками 5, 1, 8, 2, 6 (подходит 4: 2, 5, 6, 8); `minStock = 2`, страница 0 размера 3 с `Sort.by("stockQuantity")` — [2, 5, 6]; та же страница по убыванию — [8, 6, 5]; вторая страница в порядке возрастания — [8] и `isLast()`; в SQL-логе `order by stock_quantity asc/desc` меняется между вызовами при том же методе; в комментарии: Sort — «данные», а не «код».
- Подсказка (не решение): `PageRequest.of(page, size, Sort)` — сортировка едет вместе с пагинацией в одном объекте; сам SQL метода от неё не зависит.
