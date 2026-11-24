# Задание 05. Фильтр + сортировка + пагинация вместе
- Тема: Пагинация (Page / Slice / Pageable / Sort)
- Difficulty: 5
- Что нужно сделать: Добавьте в `ProductRepository` derived query: товары, у которых имя содержит фрагмент (без учёта регистра), возвращающий `Page<Product>` с параметром `Pageable` (с `Sort`). Тест создан заранее (TASK05Test): все три механизма работают одновременно — подмножество отфильтровано, отсортировано по цене desc и обойдено по страницам.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // метод должен иметь точно такую сигнатуру:
    Page<Product> findByNameContainingIgnoreCase(String fragment, Pageable pageable);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t08.TASK05Test#filterSortAndPagingInOneCall
```
- Критерий приёмки: Тест: в результат попадают только «Alpha» (3 из 5); порядок внутри страниц — глобально по цене desc (30, 20, затем 10 — не пересортировка каждой страницы отдельно); `totalElements == 3`; в SQL-логе в одном запросе данных видны И `like`, И `order by price desc`, И `fetch first ? rows only`; count-запрос содержит `like`, но без `order by`.
- Подсказка (не решение): фрагмент — предикат, `Sort` — в `Pageable`, `size`/`offset` — тоже в `Pageable`: три роли, один вызов.
