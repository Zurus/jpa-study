# Задание 06. Constructor expression (DTO-проекция)
- Тема: JPQL @Query
- Difficulty: 6
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query` с constructor expression, возвращающий `List<ProductNamePrice>` (record из пакета `com.example.jpalab.dto`) для товаров дороже заданной цены. Тест, проверяющий содержимое записей, создан заранее (TASK06Test). Метод с нужным @Query УЖЕ ЕСТЬ в ProductRepository — используйте его имя `namePricesAtLeast`.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (см. «Что нужно сделать»)
    // метод с таким @Query уже существует в репозитории, имя — namePricesAtLeast:
    List<ProductNamePrice> namePricesAtLeast(@Param("minPrice") BigDecimal minPrice);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t02.TASK06Test#constructorExpressionProjectsNameAndPrice
```
- Критерий приёмки: Тест: возвращаемый тип — список record-ов (не `Object[]`); поля record заполнены корректно; в SQL-логе выбраны только `name, price`; тест-ловушка в комментарии: если в `new` указать неверное полное имя класса — контекст Spring не поднимется (ошибка на старте), что фиксируется отдельным комментарием в тесте.
- Подсказка (не решение): после `new` — полное имя класса с параметрами, совпадающими с конструктором record по количеству, типу и ПОРЯДКУ.
