# Тема 17. Entity Inheritance (наследование сущностей)

Полиморфные иерархии в JPA: общий базовый класс, три стратегии
маппинга (`SINGLE_TABLE`, `JOINED`, `TABLE_PER_CLASS`),
дискриминатор и полиморфные запросы по корню иерархии.

## Что в теме

- `@MappedSuperclass`: общие поля без отдельной таблицы;
- переход от «каждый подкласс — своя таблица» к иерархии
  с полиморфным поведением;
- `@Inheritance` + `@DiscriminatorColumn` + `@DiscriminatorValue`;
- стратегия `SINGLE_TABLE`: одна общая таблица, полиморфный запрос;
- стратегия `JOINED`: корневая таблица + таблицы подклассов с FK;
- стратегия `TABLE_PER_CLASS`: полностью независимые таблицы;
- полиморфные JPQL-запросы по корню и по конкретному подклассу;
- стоимость persist под каждой стратегией (Hibernate Statistics).

## Стартовый код (уже в проекте)

- `com.example.jpalab.entity.BaseEntity` — `@MappedSuperclass`
  (`id` — IDENTITY, `createdAt`);
- `com.example.jpalab.entity.DigitalProduct` — `@Entity`
  (поле `downloadUrl`), в стартовом состоянии — своя таблица
  `digital_product`;
- `com.example.jpalab.entity.PhysicalProduct` — `@Entity`
  (поле `weight`), в стартовом состоянии — своя таблица
  `physical_product`;
- Сервис-скелет `com.example.jpalab.service.ProductHierarchyService`
  (только `EntityManager`, методов нет — их добавляют вы).

> Важно: в заданиях 03–06 иерархию `BaseEntity` нужно преобразовать
> в SINGLE_TABLE. После этого `BaseEntity` больше не является
> `@MappedSuperclass` — не забудьте удалить эту аннотацию.

## Границы темы

- Иерархия `BaseEntity → DigitalProduct / PhysicalProduct` и две
  дополнительные иерархии: `vehicle` (JOINED) и `document`
  (TABLE_PER_CLASS).
- Сущность `Product` (темы 01–16) **не изменять**.
- Без `@ElementCollection` (тема 15) и `@ManyToMany` (тема 16).
- Без дублирования жизненного цикла (тема 11): здесь — только
  маппинг иерархии и запросы.
- Без `@SecondaryTable`, `@EmbeddedId`, discriminator-стратегий
  `EXCLUSIVE`/`READ`/`NONE`.

## Задания

| # | Файл | Артефакт | Сложность |
|---|------|----------|-----------|
| 01 | [TASK-01.md](TASK-01.md) | `saveDigitalProduct(String, LocalDateTime)` | 1 |
| 02 | [TASK-02.md](TASK-02.md) | `savePhysicalProduct(Double, LocalDateTime)` | 2 |
| 03 | [TASK-03.md](TASK-03.md) | SINGLE_TABLE для `BaseEntity` (аннотации) | 3 |
| 04 | [TASK-04.md](TASK-04.md) | `@DiscriminatorValue` для подклассов | 4 |
| 05 | [TASK-05.md](TASK-05.md) | `findAllProducts()` | 5 |
| 06 | [TASK-06.md](TASK-06.md) | `findPhysicalProductIdsByWeightAtLeast(Double)` | 6 |
| 07 | [TASK-07.md](TASK-07.md) | иерархия `Vehicle/Car/Bus`, JOINED | 7 |
| 08 | [TASK-08.md](TASK-08.md) | иерархия `Document/PdfDocument/OdtDocument`, TABLE_PER_CLASS | 8 |
| 09 | [TASK-09.md](TASK-09.md) | `findDigitalProductIdsWithoutDownloadUrl()` | 9 |
| 10 | [TASK-10.md](TASK-10.md) | `persistStatementCounts()` | 10 |

Проверка каждого задания — точная команда в его файле.

Вся тема: `mvn -Dtest="com.example.jpalab.tasks.t17.TASK*Test"`.
