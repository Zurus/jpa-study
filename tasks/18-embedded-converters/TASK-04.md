# TASK-04. Enum как номер: @Enumerated(EnumType.ORDINAL)

Тема: Embedded-типы и конвертеры. Сложность: 4/10.

## Что делать

Добавить в `com.example.jpalab.entity.Offer` поле
`lastKnownStatus` типа `ProductStatus` с маппингом
`@Enumerated(EnumType.ORDINAL)` (колонка по умолчанию —
`last_known_status`).

В колонку должны попадать номера констант enum:
`ACTIVE` = 0, `INACTIVE` = 1, `DISCONTINUED` = 2.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t18.TASK04Test#ordinalEnumStoresNumbers
```

## Критерии

- для трёх сохранённых `Offer` с `lastKnownStatus`
  `ACTIVE` / `INACTIVE` / `DISCONTINUED` в колонке
  `last_known_status` лежат числа 0, 1, 2 (не имена);
- остальные поля `Offer` не пострадали.

## Подсказка

ORDINAL хранит порядковый номер константы в классе — сравните,
что попадает в колонку при `EnumType.STRING` и что при
`EnumType.ORDINAL`, и почему пересортировка enum ломает
данные, сохранённые как ORDINAL.
