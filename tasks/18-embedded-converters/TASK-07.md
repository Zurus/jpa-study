# TASK-07. Money в одной текстовой колонке

Тема: Embedded-типы и конвертеры. Сложность: 7/10.

## Что делать

Создать конвертер
`MoneyTextConverter implements AttributeConverter<Money, String>`
(любой пакет в `src/main`): `Money` → строка вида
`"сумма валюта"` через один пробел (например, `"42.50 USD"`),
строка → `Money` обратно.

Добавить в `com.example.jpalab.entity.Offer` поле
`total` типа `Money` и применить к нему конвертер через
`@Convert` (колонка — `total`).

Внимание: это НЕ `@Embedded` — `Money` здесь занимает ровно
одну текстовую колонку, а не две.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t18.TASK07Test#converterStoresMoneyInSingleColumn
```

## Критерии

- для сохранённого `Offer` с `total = Money(42.50, USD)` в
  колонке `offer.total` лежит строка `"42.50 USD"`;
- в таблице `OFFER` ровно одна колонка `TOTAL`.

## Подсказка

`AttributeConverter` может маппить и целый объект в одну
колонку — JPA передаёт в него Java-тип поля, а не колонку.
