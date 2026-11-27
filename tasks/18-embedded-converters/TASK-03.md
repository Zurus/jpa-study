# TASK-03. Второй вложенный Money — свои колонки

Тема: Embedded-типы и конвертеры. Сложность: 3/10.

## Что делать

Добавить в `com.example.jpalab.entity.Offer` поле
`discountedPrice` типа `Money` (`@Embedded`) и настроить ему
собственные колонки `discount_amount` (для `amount`) и
`discount_currency` (для `currency`).

Важно: два `@Embedded` одного типа без переопределения пишут
в одни и те же колонки — убедитесь, что `basePrice`
(задание 02) и `discountedPrice` используют разные колонки.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t18.TASK03Test#secondEmbeddedMoneyUsesOwnColumns
```

## Критерии

- для сохранённого `Offer` в одной строке таблицы `offer`
  читаются все четыре значения: `base_amount`, `base_currency`,
  `discount_amount`, `discount_currency`;
- значения `basePrice` и `discountedPrice` не перепутаны.

## Подсказка

Конфликт имён колонок двух вложенных типов одного класса —
классическая ошибка маппинга; лечится `@AttributeOverrides`
на каждом из полей.
