# TASK-02. Переименовать колонки вложенного типа

Тема: Embedded-типы и конвертеры. Сложность: 2/10.

## Что делать

Добавить на поле `basePrice` в `com.example.jpalab.entity.Offer`
`@AttributeOverrides` так, чтобы колонки вложенного `Money`
назывались `base_amount` (для `amount`) и `base_currency`
(для `currency`).

Дефолтные колонки `amount` и `currency` должны исчезнуть
из таблицы `offer`.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t18.TASK02Test#basePriceColumnsRenamedByAttributeOverrides
```

## Критерии

- сохранённый `Offer` читается: `basePrice.amount` и
  `basePrice.currency` на месте;
- в `INFORMATION_SCHEMA.COLUMNS` для таблицы `OFFER` есть
  колонки `BASE_AMOUNT` и `BASE_CURRENCY`;
- колонок `AMOUNT` и `CURRENCY` в таблице `OFFER` больше нет.

## Подсказка

`@AttributeOverride` ссылается на имя ПОЛЯ вложенного класса
(`amount`, `currency`), а не на старое имя колонки.
