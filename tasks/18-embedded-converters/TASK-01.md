# TASK-01. Сохранить Offer с вложенным Money

Тема: Embedded-типы и конвертеры. Сложность: 1/10.

## Что делать

Добавить метод в `com.example.jpalab.service.OfferService`:

```java
public Offer saveOffer(String name, Money basePrice, ProductStatus status) {
    // TODO
}
```

Метод создаёт `Offer` с переданными `name`, `basePrice`
(`Money` — `@Embeddable`) и `status`, сохраняет его и возвращает
объект с назначенным `id`.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t18.TASK01Test#saveOfferRoundTripsEmbeddedPrice
```

## Критерии

- возвращаемый объект — `Offer` с непустым `id`;
- после `em.clear()` и `find(Offer.class, id)` читаются
  переданные `name`, `basePrice.amount`, `basePrice.currency`
  и `status`;
- в таблице `offer` есть ровно одна строка для этого `id`.

## Подсказка

`@Embedded` не требует отдельной таблицы: поля вложенного типа
просто пишутся в колонки таблицы сущности, в которую он встроен.
