# TASK-01. Цифровой товар: наследованные поля сохраняются

Тема: Entity Inheritance. Сложность: 1/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductHierarchyService`:

```java
public DigitalProduct saveDigitalProduct(String downloadUrl, LocalDateTime createdAt) {
    // TODO
}
```

Метод создаёт `DigitalProduct` с переданными `downloadUrl` и
`createdAt` (поле `createdAt` наследуется от `BaseEntity`),
сохраняет его и возвращает объект с назначенным `id`.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t17.TASK01Test#saveDigitalProductRoundTripsBaseFields
```

## Критерии

- возвращаемый объект — `DigitalProduct` с непустым `id`;
- после `em.clear()` и `find(DigitalProduct.class, id)` в БД
  читаются переданные `downloadUrl` и `createdAt`.

## Подсказка

`id` и `createdAt` объявлены в `BaseEntity` — у подкласса они
доступны как обычные поля. Стратегии наследования здесь ещё не
нужны: подкласс уже самостоятельная сущность.
