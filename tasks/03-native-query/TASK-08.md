# Задание 08. NULL в native SQL
- Тема: Native queries
- Difficulty: 8
- Что нужно сделать: Добавьте в `ProductRepository` native-запрос, возвращающий товары без категории (`category_id IS NULL`). Отдельно в тесте (динамический запрос) проверяется поведение условия `category_id = :id` при значении `null`. Тест создан заранее (TASK08Test). На 3 товарах: 2 с категорией, 1 без.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с native-запросом (nativeQuery = true, см. «Что нужно сделать»)
    // метод должен иметь точно такое имя (без параметров):
    List<Product> findNativeWithoutCategory();
}

// В тесте также есть динамический запрос:
// em.createNativeQuery("SELECT * FROM products WHERE category_id = :id", Product.class)
// с setParameter("id", null)```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t03.TASK08Test#nullSemantics
```
- Критерий приёмки: Тест: метод с `IS NULL` возвращает ровно 1 товар; условие `= :id` с null-параметром возвращает ПУСТОЙ список (0 строк), а не товары без категории — в комментарии зафиксирована SQL-семантика: `NULL = NULL` не является TRUE; в SQL-логе видно `is null` и `= ?` соответственно.
- Подсказка (не решение): сравнение с NULL в SQL — отдельная операция IS NULL; `= null` молча не находит ничего.
