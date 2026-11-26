# Задание 01. Проекция полей: скалярный кортеж (name, price)
- Тема: Проекции
- Difficulty: 1
- Что нужно сделать: В `ProductRepository` добавьте JPQL-метод `List<Object[]> namesAndPricesAbove(@Param("minPrice") BigDecimal minPrice)`. Запрос возвращает только два скалярных поля — имя и цену товара (порядок столбцов в кортеже: [0] name, [1] price) — для товаров со СТРОГО большей ценой, чем `:minPrice` (товар с ценой ровно равной границе не попадает). Сортировка: цена по убыванию, тай-брейк — id по возрастанию.
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Query — скалярный SELECT (name, price), строго больший :minPrice,
//       ORDER BY price DESC, id ASC
List<Object[]> namesAndPricesAbove(@Param("minPrice") BigDecimal minPrice);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t10.TASK01Test#scalarTupleProjectionOrderByPriceDesc
```
- Критерий приёмки: Тест: 5 товаров (50.00, 100.00, 150.00, 250.00, 250.00), вызов с 100.00 — в выборке 3 кортежа: сначала два товара по 250.00 (High B раньше High A — у него меньший id), затем Mid 150.00; `Boundary` (ровно 100.00) отсутствует.
- Подсказка (не решение): для `List<Object[]>` в SELECT перечисляются именно поля сущности, а не сама сущность; «строго больше» — это `>`, а не `>=`.
