# Задание 08. N+1 под пагинацией
- Тема: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
- Difficulty: 8
- Что нужно сделать: Тест создан заранее (TASK08Test): 6 товаров / 6 категорий. Загружается ПЕРВАЯ страница (`PageRequest.of(0, 2)`) обычных `findAll(Pageable)`, в цикле по странице читаются категории. Посчитано SQL и показано: пагинация НЕ лечит N+1 — на каждой странице свои N запросов к ассоциациям (плюс count-запрос). Методы в репозиторий добавлять не нужно.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t07.TASK08Test
// Ключевые шаги:
//   6/6; em.flush(); em.clear();
//   statistics.clear();
//   Page<Product> page = productRepository.findAll(PageRequest.of(0, 2)); // count + data = 2
//   for (Product p : page.getContent()) p.getCategory().getName();        // +2 (N = размер страницы)
//   итого 4
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t07.TASK08Test#paginationDoesNotFixNPlus1
```
- Критерий приёмки: Тест: вызов страницы — 2 prepared statement (count + данные); доступ к категориям элементов страницы добавляет ровно N = 2 (размер страницы) SELECT-ов; итого 4; в комментарии зафиксировано: N в «N+1» при пагинации = размер страницы, и на каждой странице он повторяется — решение: `@EntityGraph`/fetch join вместе с `Pageable` (задание темы 8).
- Подсказка (не решение): пагинация уменьшает число СТРок, но не отменяет ленивую инициализацию по каждой строке страницы.
