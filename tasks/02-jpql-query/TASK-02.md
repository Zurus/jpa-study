# Задание 02. Детерминированная сортировка с тай-брейком
- Тема: JPQL @Query
- Difficulty: 2
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`: все товары с ценой больше заданной, отсортированные по цене по убыванию, а при равных ценах — по id по возрастанию. Тест с ДВУМА товарами одинаковой цены создан заранее (TASK02Test).
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (см. «Что нужно сделать»)
    // метод должен иметь точно такую сигнатуру:
    List<Product> findPricedAboveSortedDeterministic(@Param("min") BigDecimal min);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t02.TASK02Test#deterministicOrderWithIdTieBreaker
```
- Критерий приёмки: Тест: порядок результата детерминирован — товар с ценой 200 первый, два товара с ценой 100 идут по возрастанию id (проверить именно id, а не имя); тест повторяем и проходит стабильно. В SQL-логе `order by price desc, id asc`.
- Подсказка (не решение): вторичный ключ сортировки — первичный ключ; без него порядок при равных значениях не гарантирован.
