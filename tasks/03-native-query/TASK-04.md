# Задание 04. JOIN в native SQL
- Тема: Native queries
- Difficulty: 4
- Что нужно сделать: Добавьте в `ProductRepository` native-запрос с JOIN: товары категории, переданной по имени — через явный `JOIN ... ON` по внешнему ключу. Возврат `List<Product>`. Тест с категорией и товаром без категории создан заранее (TASK04Test).
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с native-запросом (nativeQuery = true, см. «Что нужно сделать»)
    // метод должен иметь точно такую сигнатуру:
    List<Product> findNativeByCategoryName(@Param("categoryName") String categoryName);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t03.TASK04Test#nativeJoinByForeignKey
```
- Критерий приёмки: Тест: запрос по «Gadgets» возвращает 2 товара; товар без категории в любом случае не возвращается (INNER JOIN); в SQL-логе JOIN собран явно: `join categories ... on` с колонкой внешнего ключа — никакой магии модели сущностей; поля возвращённых сущностей заполнены полностью.
- Подсказка (не решение): в native SQL связь таблиц описывается через `ON` с реальным именем колонки-ключа.
