# TASK-02. Физический товар: оба подкласса сосуществуют

Тема: Entity Inheritance. Сложность: 2/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductHierarchyService`:

```java
public PhysicalProduct savePhysicalProduct(Double weight, LocalDateTime createdAt) {
    // TODO
}
```

Метод создаёт `PhysicalProduct` с переданными `weight` и
`createdAt`, сохраняет его и возвращает объект с назначенным `id`.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t17.TASK02Test#savePhysicalProductCoexistsWithDigital
```

## Критерии

- возвращаемый объект — `PhysicalProduct` с непустым `id`,
  отличным от `id` сохранённого ранее `DigitalProduct`;
- после `em.clear()` и `find` оба объекта читаются из БД
  (цифровой и физический товары живут одновременно).

## Подсказка

В стартовом состоянии у каждого подкласса своя таблица
(`digital_product`, `physical_product`) — это штатное поведение
подклассов `@MappedSuperclass`, никакой магии не требуется.
