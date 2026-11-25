# Задание 05. Коллекционный параметр `IN :names`
- Тема: @Modifying
- Difficulty: 5
- Что нужно сделать: Добавить в `ProductRepository` метод с точной сигнатурой из «Стартового кода»: bulk UPDATE (`@Modifying` + `@Query`), устанавливающий заданный stock всем товарам, чьё имя входит в список (WHERE p.name IN :names). Метод возвращает число обновлённых строк. Тест уже создан (`TASK05Test`): четыре товара A, B, C, D без категории, у всех stock 0; список [A, C] и stock 7.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте метод с этой сигнатурой:
    // @Modifying + @Query с условием IN :names (см. «Что нужно сделать»)
    int setStockByNames(@Param("names") List<String> names, @Param("stock") int stock);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t09.TASK05Test#inListParameter
```
- Критерии приёмки: Тест `inListParameter` зелёный: метод возвращает 2; в БД (native SELECT) stock: A = 7, C = 7, B = 0, D = 0; в SQL-логе один `update products` с `in (?, ?)`.
- Подсказка (не решение): Hibernate сам расширяет List-параметр в `IN :names` на нужное количество плейсхолдеров — писать цикл или строковый SQL не нужно.
