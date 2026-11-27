# TASK-09. Изолированная внутренняя граница: «откат только внутренней части»

Тема: `@Transactional`: propagation и rollback. Сложность: 9/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductService`:

```java
@Transactional
public boolean transferStockSafely(Long fromId, Long toId, int amount) {
    // TODO
}
```

Поведение:

- сам метод работает во внешней `@Transactional` транзакции;
- перенос `amount` выполняется в отдельной ВНУТРЕННЕЙ транзакции с
  propagацией REQUIRES_NEW — декларативно (отдельный метод с
  `@Transactional(propagation = Propagation.REQUIRES_NEW)` + вызов через
  прокси самого сервиса) или программно (`TransactionTemplate` с явной
  `DefaultTransactionDefinition(Propagation.REQUIRES_NEW.value())`);
- внутри: сначала прибавить `amount` получателю и `flush`; затем в
  памяти снять `amount` у источника; если остаток уйдёт в минус —
  `IllegalArgumentException` ДО flush источника (помните
  `@PositiveOrZero`);
- сбой внутренней транзакции откатывает только её изменения, включая
  уже записанный UPDATE получателя;
- `transferStockSafely` перехватывает сбой, сохраняет `Order`
  (логирует попытку по источнику) и возвращает `false`;
- при успехе возвращает `true`; `Order` сохраняется в обоих случаях и
  закоммичен внешней транзакцией.

Важно: уровень NESTED в этом стеке недоступен — `JpaTransactionManager`
не поддерживает savepoints (проверено экспериментально), Hibernate 6 не
имеет `Session.savepoint`. Семантика «вложенный» отката достигается
внутренней REQUIRES_NEW-транзакцией.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t19.TASK09Test#innerBoundaryRollsBackOnlyInnerPart
```

## Критерии

- случай A (склады 10 и 5, перенос 4): возвращает `true`, в БД 6 и 9,
  `Order` создан;
- случай B (источник 2, тот же получатель — 9, перенос 5): возвращает
  `false`, источник остался 2, получатель остался 9 (UPDATE получателя
  откатился вместе с внутренней транзакцией), `Order` создан;
- во внешнюю транзакцию сбой внутренней не «пробрасывается»:
  внешний коммит происходит в обоих случаях.

## Подсказка

Ключ — ГРАНИЦА: всё, что между открытием внутренней транзакции и её
commit/rollback, живёт своей жизнью. Внешний метод видит только
«успех/сбой» и решает, что делать дальше (лог, флаг, повтор).
