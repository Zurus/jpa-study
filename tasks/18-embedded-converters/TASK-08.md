# TASK-08. Вложенный @Embeddable с overrides внутри

Тема: Embedded-типы и конвертеры. Сложность: 8/10.

## Что делать

Создать `com.example.jpalab.entity.PriceRange`
(именно этот FQCN) — `@Embeddable` с полями
`min` и `max`, оба типа `Money` и оба `@Embedded`.

Внутри класса `PriceRange` переопределить колонки:
`min.amount` → `min_amount`, `min.currency` → `min_currency`,
`max.amount` → `max_amount`, `max.currency` → `max_currency`.

Добавить в `com.example.jpalab.entity.Offer` поле
`priceRange` типа `PriceRange` с `@Embedded`.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t18.TASK08Test#nestedEmbeddableWithInternalOverrides
```

## Критерии

- для сохранённого `Offer` в таблице `offer` читаются
  `min_amount = 5.00`, `min_currency = EUR`,
  `max_amount = 50.00`, `max_currency = EUR`;
- без `@AttributeOverrides` внутри `PriceRange` два вложенных
  `Money` снова запишутся в общие колонки.

## Подсказка

`@AttributeOverrides` разрешено писать на `@Embeddable`-классе
самом (тогда они действуют на все его вхождения) или на каждом
поле-`@Embedded` отдельно.
