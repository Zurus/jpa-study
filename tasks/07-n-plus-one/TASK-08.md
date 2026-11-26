# Задание 08. N+1 под пагинацией
- Тема: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
- Difficulty: 8
- Что нужно сделать: Реализуйте в `ProductService` метод `List<String> pageProductRowsWithCategoryNames(Pageable pageable)` — загрузить СТРАНИЦУ товаров `findAll(pageable)` и собрать строки вида `"<name> [<categoryName>]"` по элементам страницы. Заранее созданный тест (TASK08Test) показывает: пагинация НЕ лечит N+1 — за страницу: count-запрос + data-запрос + по одному SELECT на категорию каждого элемента (N = размер страницы).
- Стартовый код:
```java
// com/example/jpalab/service/ProductService.java

// TODO: productRepository.findAll(pageable) + цикл по getContent():
//       name + " [" + category.getName() + "]"
List<String> pageProductRowsWithCategoryNames(Pageable pageable);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t07.TASK08Test#paginationDoesNotFixNPlus1
```
- Критерий приёмки: Тест: 6 товаров / 6 категорий, страница `PageRequest.of(0, 2)`; ваш метод возвращает ровно строки двух первых товаров (по id); всего 4 prepared statement (count + данные + 2 lazy по категориям); в комментарии зафиксировано: N в «N+1» при пагинации = размер страницы и повторяется на каждой странице — решение: `@EntityGraph`/fetch join вместе с `Pageable` (задания темы 08).
- Подсказка (не решение): пагинация уменьшает число СТРОК, но не отменяет ленивую инициализацию по каждой строке страницы.
