# TASK-05. REQUIRED (дефолт): присоединение и смерть вместе с вызывающим

Тема: `@Transactional`: propagation и rollback. Сложность: 5/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductService`:

```java
@Transactional
public void updateProductPrice(Long id, BigDecimal newPrice) {
    // TODO
}
```

Изменить цену товара `id` на `newPrice`. Аннотация — стандартный
`@Transactional` без атрибутов (дефолтный уровень propagation
REQUIRED).

Требование: если транзакция уже открыта — метод присоединяется к ней и
НЕ коммитится сам: изменение живёт и умирает вместе с транзакцией
вызывающего.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t19.TASK05Test#requiredJoinsCallerAndDiesWithIt
```

## Критерии

- тест вызывает метод внутри `TransactionTemplate`, затем внешняя
  транзакция срывается (rollback);
- после этого цена в БД осталась прежней (изменение не закоммичено
  отдельно от внешней транзакции);
- контраст с заданием 03: та же схема, другой уровень propagation —
  другой исход.

## Подсказка

REQUIRED — самый частый уровень: «если транзакции нет — открой, если
есть — работай в ней». Отдельного commit у присоединившегося метода
нет: коммит делает только собственник транзакции.
