# Задание 07. Ловушка: constructor expression в native не работает
- Тема: Native queries
- Difficulty: 7
- Что нужно сделать: Добавьте в `ProductRepository` native-метод `List<Object[]> nativeAllNamePrice()`: `@Query(nativeQuery = true)`, возвращающий колонки name, price для ВСЕХ товаров, отсортированных по имени. Заранее созданный тест (TASK07Test) показывает, что динамический native-запрос с `SELECT new ...` не работает (конструкторное выражение — синтаксис JPQL), а корректный путь — ваш native-метод с ручным маппингом `Object[]` в record `ProductNamePrice`.
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Query(nativeQuery = true) — SELECT name, price FROM products ORDER BY name
List<Object[]> nativeAllNamePrice();
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t03.TASK07Test#constructorExpressionInNative
```
- Критерий приёмки: Тест: вариант (а) — `assertThrows` для динамического `createNativeQuery("SELECT new ...")` (исключение/ошибка парсинга); вариант (б) — ваш метод `nativeAllNamePrice()` на тех же данных возвращает корректные record-ы (имена и цены совпадают по номиналу); в комментарии зафиксировано: `SELECT new ...` — синтаксис JPQL, в native-запросе DTO собирается вручную из `Object[]`/скаляров.
- Подсказка (не решение): native-запрос — это SQL для драйвера, а не язык сущностей: конструкторов в нём не существует.
