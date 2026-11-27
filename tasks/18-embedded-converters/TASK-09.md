# TASK-09. Переопределение с атрибутами колонки

Тема: Embedded-типы и конвертеры. Сложность: 9/10.

## Что делать

Расширить переопределение `basePrice.currency`
(задание 02) в `com.example.jpalab.entity.Offer`: колонка
`base_currency` должна быть VARCHAR(3) и NOT NULL — через
`@Column(name = "base_currency", length = 3, nullable = false)`
внутри `@AttributeOverride`.

Остальные переопределения темы (задания 02–03, 08) остаются
без изменений.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t18.TASK09Test#attributeOverrideCarriesColumnAttributes
```

## Критерии

- в `INFORMATION_SCHEMA.COLUMNS` для таблицы `OFFER` колонка
  `BASE_CURRENCY`: `CHARACTER_MAXIMUM_LENGTH = 3` и
  `IS_NULLABLE = 'NO'`;
- все прежние задания темы 18 остаются зелёными.

## Подсказка

`@AttributeOverride` несёт не только имя: вложенный `@Column`
передаёт length, precision, scale, nullable — всё, что
обычно ставится на поле сущности.
