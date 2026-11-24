# Задание 09. Подзапрос IN
- Тема: JPQL @Query
- Difficulty: 9
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`, находит товары, категория которых входит в подмножество категорий, чьи имена совпадают с паттерном LIKE (например `Book%`): внешний запрос по `Product`, подзапрос по `Category` внутри `IN (...)`. Тест с «ловушкой имён» создан заранее (TASK09Test).
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (см. «Что нужно сделать»)
    // метод должен иметь точно такую сигнатуру:
    List<Product> findProductsInCategoriesLike(@Param("pattern") String pattern);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t02.TASK09Test#inSubqueryByCategoryNamePattern
```
- Критерий приёмки: Тест: паттерн `Book%` возвращает товары обеих «книжных» категорий (2 категории → N товаров), «Notebooks» не попадает; товар БЕЗ категории никогда не возвращается (подзапрос по association — семантика внутреннего соединения); в SQL-логе подзапрос реализован отдельным join/вложенным запросом к `categories`.
- Подсказка (не решение): подзапрос должен возвращать тип, сопоставимый со значением ассоциации (здесь — сущности `Category`).
