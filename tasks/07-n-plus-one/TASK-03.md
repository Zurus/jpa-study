# Задание 03. Лечение JOIN FETCH (to-one)
- Тема: N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
- Difficulty: 3
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query` с `JOIN FETCH p.category`, возвращающий все товары. Тест создан заранее (TASK03Test): 5 товаров / 5 категорий; доказывается, что товары и категории загружаются ОДНИМ запросом, и что после него `getCategory()` у каждого товара уже инициализирован (дополнительных SELECT-ов нет).
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JOIN FETCH (см. «Что нужно сделать»)
    // метод должен иметь точно такое имя (аргументов нет):
    List<Product> findAllWithCategoryJoined();
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t07.TASK03Test#joinFetchLoadsCategoryInSingleQuery
```
- Критерий приёмки: Тест: метод выполнил ровно 1 prepared statement; в SQL-логе одна `select ... from products p1_0 join categories c1_0 on ...`; после загрузки у всех товаров `Hibernate.isInitialized(category) == true`; доступ к `getCategory()` в цикле не добавляет ни одного запроса (дифференциал 0).
- Подсказка (не решение): `JOIN FETCH p.category` — Hibernate собирает сущность и ассоциацию в одной SELECT с join.
