# Задание 03. Явный JOIN к ассоциации
- Тема: JPQL @Query
- Difficulty: 3
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`: товары заданной категории (по имени категории) с явным `JOIN` к ассоциации `category`. Тест создан заранее (TASK03Test): категория с 2 товарами и один товар без категории.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (см. «Что нужно сделать»)
    // метод должен иметь точно такую сигнатуру:
    List<Product> findProductsOfCategory(@Param("categoryName") String categoryName);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t02.TASK03Test#explicitJoinByCategoryName
```
- Критерий приёмки: Тест: запрос накатываем на «Gadgets» — возвращает 2 товара, «Books» — пусто (товар без категории не возвращается, это INNER JOIN); в SQL-логе виден `join categories` в одном SELECT; полученный `product.getCategory()` уже инициализирован (дополнительных SELECT по категории нет — проверить статистикой или `Hibernate.isInitialized`).
- Подсказка (не решение): `JOIN p.category c` — `c` станет алиасом категории, к нему и пишется условие.
