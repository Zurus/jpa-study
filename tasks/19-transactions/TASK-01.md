# TASK-01. Перенос склада с атомарностью по умолчанию

Тема: `@Transactional`: propagation и rollback. Сложность: 1/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductService`:

```java
@Transactional
public void transferStock(Long fromId, Long toId, int amount) {
    // TODO
}
```

Перенос `amount` единиц со склада источника на склад получателя в одной
транзакции. Порядок операций:

1. прибавить `amount` на склад получателя и `flush` (записать UPDATE);
2. в памяти снять `amount` со склада источника;
3. если остаток источника ушёл в минус — выбросить
   `IllegalArgumentException` (проверка — ДО flush источника; у поля
   `stockQuantity` стоит `@PositiveOrZero`, записать отрицательное
   значение не получится — валидатор сработает первым);
4. иначе — `flush` и завершить транзакцию.

Требование: при сбое транзакция откатывается целиком, включая уже
записанный UPDATE получателя.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t19.TASK01Test#transferStockRollsBackPartialUpdateOnInsufficientStock
```

## Критерии

- склад 10/5, перенос 4: после возврата метода БД хранит 6 и 9;
- склад 3/0, перенос 5: метод бросает `IllegalArgumentException`, в БД
  остаются 3 и 0 (UPDATE получателя, записанный до срыва, откатился);
- тест читает БД нативным SELECT в отдельной транзакции.

## Подсказка

`@Transactional` без атрибутов — дефолтный сценарий: unchecked-
исключение (наследник `RuntimeException`) откатывает транзакцию.
«Частичный» результат виден только внутри сессии; наружу он выходит
только если кто-то его закоммитил.
