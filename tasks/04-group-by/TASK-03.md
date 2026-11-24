# Задание 03. GROUP BY через ассоциацию
- Тема: GROUP BY / HAVING
- Difficulty: 3
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`: количество товаров в каждой категории — группировка по `p.category.name` (свойство-путь через ассоциацию), без явного JOIN. Возврат `Object[]` [название категории, количество]. Тест создан заранее (TASK03Test): две категории с 2 и 1 товаром.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (GROUP BY / агрегаты, см. «Что нужно сделать»)
    // метод должен иметь точно такое имя (без параметров):
    List<Object[]> countByCategoryName();
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t04.TASK03Test#groupByAssociationPath
```
- Критерий приёмки: Тест: строки ["Books", 1] и ["Gadgets", 2]; в SQL-логе Hibernate САМ добавил join к `categories` (implicit join для свойства-пути) — группа строится по колонке таблицы категорий; в комментарии зафиксировано, как Hibernate транслирует `p.category.name` в SQL.
- Подсказка (не решение): точка в выражении GROUP BY — это свойство-путь, JOIN Hibernate добавит сам.
