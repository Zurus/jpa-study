# TASK-06. Авто-конвертер: @Converter(autoApply = true)

Тема: Embedded-типы и конвертеры. Сложность: 6/10.

## Что делать

Создать конвертер
`DiscountTypeConverter implements AttributeConverter<DiscountType, String>`
(любой пакет в `src/main`): `PERCENT` → `"P"`, `FIXED` → `"F"`,
обратно по тем же кодам.

Пометить класс `@Converter(autoApply = true)`. Поле
`discountType` в `Offer` аннотировать НЕ нужно: авто-конвертер
применяется ко всем постоянным полям этого типа.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t18.TASK06Test#autoAppliedConverterNeedsNoAnnotation
```

## Критерии

- для сохранённых `Offer` с `discountType = PERCENT` и `FIXED`
  в колонке `offer.discount_type` лежат `"P"` и `"F"`;
- на поле `discountType` нет ни `@Convert`, ни `@Enumerated`.

## Подсказка

Область действия `autoApply` — весь persistence unit:
проверьте, что это не влияет на другие enum-типы (например,
`ProductStatus`).
