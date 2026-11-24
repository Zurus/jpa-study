# Задание 07. Пагинация Pageable в derived query
- Тема: Derived queries (вывод запроса из имени метода)
- Difficulty: 7
- Что нужно сделать: Добавьте в `ProductRepository` derived query, возвращающий `Page<Product>`: товары, у которых имя совпадает с LIKE-паттерном (без учёта регистра, например «Item%»), последний параметр — `Pageable`. Тест создан заранее (TASK07Test): он проверяет пагинацию на 6 товарах.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // метод должен иметь точно такую сигнатуру:
    Page<Product> findByNameLikeIgnoreCase(String namePattern, Pageable pageable);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t01.TASK07Test#pageableDerivedQuerySplitsItemsIntoPages
```
- Критерий приёмки: Тест: 3 страницы по 2 элемента; `getTotalElements() == 6`; последняя страница `hasNext == false`; содержимое страниц 0 и 2 не пересекается. Каждый вызов метода выполняет 2 SQL-запроса (count + данные) — зафиксировать в комментарии к тесту по SQL-логу.
- Подсказка (не решение): параметр `Pageable` в конце сигнатуры Spring Data подхватывает автоматически — в имени метода ничего явно указывать не нужно.
