# TASK-05. Конвертер enum только для одного поля

Тема: Embedded-типы и конвертеры. Сложность: 5/10.

## Что делать

Создать конвертер
`ProductStatusConverter implements AttributeConverter<ProductStatus, String>`
(любой пакет в `src/main`): в БД — имя константы в нижнем
регистре (`active`, `inactive`, `discontinued`), из БД —
обратно в enum.

Применить его к полю `status` в `com.example.jpalab.entity.Offer`
явно, через `@Convert`.

`Product.status` аннотациями не помечен и обязан остаться
в маппинге `EnumType.STRING`: в колонке `products.status` —
`DISCONTINUED` (верхний регистр), а не `discontinued`.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t18.TASK05Test#explicitConvertAppliesOnlyToAnnotatedField
```

## Критерии

- для сохранённого `Offer` с `status = INACTIVE` в колонке
  `offer.status` лежит `inactive` (нижний регистр);
- для сохранённого `Product` с `status = DISCONTINUED` в
  колонке `products.status` лежит `DISCONTINUED` (верхний
  регистр).

## Подсказка

Явный `@Convert(converter = …)` действует только на помеченное
поле — в отличие от `autoApply`, который тема разбирает в
задании 06.
