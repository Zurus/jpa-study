# Задание 07. Ловушка: constructor expression в native не работает
- Тема: Native queries
- Difficulty: 7
- Что нужно сделать: Тест создан заранее (TASK07Test): ДИНАМИЧЕСКИЕ запросы через `EntityManager`: (а) `em.createNativeQuery("SELECT new com.example.jpalab.dto.ProductNamePrice(name, price) FROM products", ...)`; (б) корректный native-запрос, возвращающий `Object[]`, с ручным маппингом в `ProductNamePrice`. Сравните поведение и зафиксируйте вывод.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t03.TASK07Test
// (a) assertThrows для "SELECT new ..." в native-запросе
// (b) SELECT name, price FROM products -> Object[] -> List<ProductNamePrice>
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t03.TASK07Test#constructorExpressionInNative
```
- Критерий приёмки: Тест: вариант (a) завершается исключением (`IllegalArgumentException`/ошибка парсинга SQL) — `assertThrows`; вариант (b) на тех же данных возвращает корректные record-ы; в комментарии зафиксировано: `SELECT new ...` — синтаксис JPQL, в native-запросе DTO собирается вручную из `Object[]`/скаляров.
- Подсказка (не решение): native-запрос — это SQL для драйвера, а не язык сущностей: конструкторов в нём не существует.
