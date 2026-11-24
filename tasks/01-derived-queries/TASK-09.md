# Задание 09. N+1: derived query + LAZY-ассоциация
- Тема: Derived queries (вывод запроса из имени метода)
- Difficulty: 9
- Что нужно сделать: Добавьте в `ProductRepository` метод derived query, возвращающий все товары с ценой больше заданной. Тест создан заранее (TASK09Test): 3 товара в разных категориях; в цикле по результату доступ к `product.getCategory().getName()`; число SELECT-запросов считается через Hibernate Statistics (счётчик prepared statements), объяснение — в комментарии к тесту.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // метод должен иметь точно такую сигнатуру:
    List<Product> findByPriceGreaterThan(BigDecimal minPrice);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t01.TASK09Test#derivedQueryWithLazyCategoryCausesNPlusOne
```
- Критерий приёмки: Тест: сам запрос выполняет 1 SELECT; доступ к LAZY-категории в цикле добавляет N SELECT (всего 1 + N = 4 для 3 товаров) — проверяется статистикой; в комментарии к тесту зафиксировано, что derived query не умеет fetch join, и указано, какие инструменты темы 2/7 с этим справляются.
- Подсказка (не решение): LAZY `@ManyToOne` — прокси; первый доступ к полю категории инициализирует его отдельным запросом.
