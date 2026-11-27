# TASK-07. JOINED: иерархия Vehicle / Car / Bus

Тема: Entity Inheritance. Сложность: 7/10.

## Что делать

Создать отдельную иерархию со стратегией `JOINED`
(пакет `com.example.jpalab.entity.vehicle`):

- `Vehicle` — корень-`@Entity`: `id` (IDENTITY),
  `createdAt: LocalDateTime`; `@Table(name = "vehicles")`,
  `@Inheritance(strategy = InheritanceType.JOINED)`,
  `@DiscriminatorColumn(name = "vtype")`;
- `Car extends Vehicle` — `seats: Integer`,
  `@Table(name = "cars")`;
- `Bus extends Vehicle` — `capacity: Integer`,
  `@Table(name = "buses")`.

Только `lombok`-геттеры/сеттеры и конструктор по умолчанию.
Методы сервиса не требуются.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t17.TASK07Test#joinedHierarchyBaseTablePlusSubclassTables
```

## Критерии

- `persist(Car)` создаёт строку и в `vehicles`, и в `cars`
  (связь по общему `id`), `vtype` заполнен;
- persist одного объекта — ровно **2** prepared statement
  (корень + подкласс);
- полиморфный запрос по `Vehicle` видит оба подкласса;
- `em.find(Vehicle.class, id)` возвращает `Car`.

## Подсказка

При JOINED запись разбивается по уровням иерархии — именно
количество prepared statement отличает её от SINGLE_TABLE.
Чтение через join выполняется одним запросом.
