# TASK-10. Сумма по пути вложенного типа

Тема: Embedded-типы и конвертеры. Сложность: 10/10.

## Что делать

Добавить метод в `com.example.jpalab.service.OfferService`:

```java
public BigDecimal sumBaseAmountByCurrency(String currency) {
    // TODO
}
```

Метод возвращает сумму `basePrice.amount` по всем `Offer`,
у которых `basePrice.currency` равна переданной валюте
(JPQL по пути через вложенный тип). Если подходящих `Offer`
нет, метод возвращает `null` (SQL SUM по пустому множеству).

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t18.TASK10Test#sumBaseAmountByCurrencyNullWhenEmpty
```

## Критерии

- два `Offer` с валютой EUR (10.00 и 25.50) и один с USD
  (100.00): `sumBaseAmountByCurrency("EUR")` = 35.50,
  `sumBaseAmountByCurrency("USD")` = 100.00;
- `sumBaseAmountByCurrency("GBP")` = `null` (не 0 и не
  исключение).

## Подсказка

Путь к полю вложенного типа в JPQL пишется через точку; SQL SUM
по пустому множеству отдаёт NULL, а Hibernate — как `null`.
