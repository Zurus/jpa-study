# TASK-02. noRollbackFor: выбранный RuntimeException не откатывает

Тема: `@Transactional`: propagation и rollback. Сложность: 2/10.

## Что делать

1. Создать исключение
   `com.example.jpalab.exception.PriceSyncException`
   (`extends RuntimeException`).
2. Добавить метод в `com.example.jpalab.service.ProductService`:

```java
@Transactional(noRollbackFor = PriceSyncException.class)
public void bulkUpdatePrices(int minStock, BigDecimal newPrice) {
    // TODO
}
```

Поведение: всем товарам с `stockQuantity >= minStock` установить цену
`newPrice` (JPQL), `flush`; затем выбросить `PriceSyncException`
(имитация сбоя «бухгалтерии» после того, как основной результат уже
готов).

Требование: несмотря на исключение, изменения цен должны быть
закоммичены — `PriceSyncException` объявлен в `noRollbackFor`.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t19.TASK02Test#noRollbackForCommitsUpdateDespiteRuntimeException
```

## Критерии

- товары со складами 5, 10 и 1; `minStock = 3`, цена `999.99`;
- метод бросает RuntimeException с именем `PriceSyncException`;
- в БД: цены первых двух товаров `999.99`, третьего — прежняя `3.00`;
- если бы исключения не было в `noRollbackFor`, весь batch откатился бы.

## Подсказка

По умолчанию Spring откатывает транзакцию при `RuntimeException` и
`Error`, а при checked-исключениях — коммитит. `noRollbackFor`
«выключает» откат только для перечисленных классов (и их наследников).
