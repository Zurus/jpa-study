# Задание 05. Одна колонка — скалярный результат
- Тема: Native queries
- Difficulty: 5
- Что нужно сделать: Добавьте в `ProductRepository` native-запрос, возвращающий `List<BigDecimal>`: только цены товаров, у которых имя равно переданному. Тест создан заранее (TASK05Test): один товар с таким именем, один — с похожим.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с native-запросом (nativeQuery = true, см. «Что нужно сделать»)
    // метод должен иметь точно такое имя и @Param:
    List<BigDecimal> nativePricesByName(@Param("name") String name);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t03.TASK05Test#singleColumnIsScalarResult
```
- Критерий приёмки: Тест: запрос по «Widget» возвращает список из одного `BigDecimal(42.50)`; по «Widget» без сущности-обёртки — сравнение `isEqualByComparingTo`; в SQL-логе выбрана ровно одна колонка `price`; в комментарии к тесту зафиксировано, что одна колонка автоматически маппится в скаляр (BigDecimal/String/Long…), а не в `Object[]`.
- Подсказка (не решение): тип возвращаемого списка определяет, во что Hibernate упакует одиночную колонку.
