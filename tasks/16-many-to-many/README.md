# Тема 16. @ManyToMany

Связь «многие-ко-многим» между двумя сущностями: `Product` ↔ `Tag`.

## Что в теме

- `@ManyToMany` + промежуточная таблица `@JoinTable`;
- владелец ассоциации и `mappedBy` (обратная, не владеющая сторона);
- добавление/удаление связей через коллекцию владельца;
- двунаправленная согласованность сторон в рамках одного persistence context;
- удаление «хвостовой» сущности (cascade = NONE по умолчанию);
- поиск через join по `@ManyToMany`;
- N+1 именно на коллекциях `@ManyToMany` и `JOIN FETCH`.

## Стартовый код (уже в проекте)

- Сущность `com.example.jpalab.entity.Tag` (`id`, `name`,
  `products: Set<Product>` — `mappedBy = "productTags"`);
- Поле-владелец в `Product`: `productTags: Set<Tag>` —
  `@ManyToMany` + `@JoinTable(name = "product_tag_link",
  joinColumns = product_id, inverseJoinColumns = tag_id)`;
- `com.example.jpalab.repository.TagRepository` (пустой);
- Сервис-сkeleton `com.example.jpalab.service.ProductTagService`
  (только `EntityManager`, методов нет — их добавляют вы).

> **Почему `productTags`, а не `tags`.** Имя `tags` в `Product` уже занято
> строковой коллекцией темы 15 (`Set<String>` + `@ElementCollection`,
> таблица `product_tags`). Поле сущностной связи поэтому называется
> `productTags`, промежуточная таблица — `product_tag_link`.

## Границы темы

- Только `@ManyToMany` между `Product` и `Tag`.
- Без `@ElementCollection` (тема 15).
- Без дублирования `@ManyToOne` (тема 05) и `@OneToMany` (тема 06) —
  здесь ровно ассоциация «многие-ко-многим» и её специфика.
- N+1 рассматривается только на коллекции `@ManyToMany` (тема 07 —
  общий N+1 по `@ManyToOne`/`@OneToMany`).
- Без `@OneToOne`, наследования, `@OrderColumn`/индексации коллекций.

## Задания

| # | Файл | Метод в `ProductTagService` | Сложность |
|---|------|------------------------------|-----------|
| 01 | [TASK-01.md](TASK-01.md) | `addTagToProduct(Long, Tag)` | 1 |
| 02 | [TASK-02.md](TASK-02.md) | `removeTagFromProduct(Long, Tag)` | 2 |
| 03 | [TASK-03.md](TASK-03.md) | `linkBothSides(Long, Tag)` | 3 |
| 04 | [TASK-04.md](TASK-04.md) | `unlinkBothSides(Long, Tag)` | 4 |
| 05 | [TASK-05.md](TASK-05.md) | `deleteTagKeepsProducts(Long)` | 5 |
| 06 | [TASK-06.md](TASK-06.md) | `findProductIdsByTagName(String)` | 6 |
| 07 | [TASK-07.md](TASK-07.md) | `findAllWithTags()` | 7 |
| 08 | [TASK-08.md](TASK-08.md) | `findProductsSharingTagWith(Long)` | 8 |
| 09 | [TASK-09.md](TASK-09.md) | `findSharedTagNames(Long, Long)` | 9 |
| 10 | [TASK-10.md](TASK-10.md) | `syncProductTags(Long, Set)` | 10 |

Проверка каждого задания:

```bash
mvn -Dtest=com.example.jpalab.tasks.t16.TASKXXTest#имяМетода
```

Вся тема: `mvn -Dtest="com.example.jpalab.tasks.t16.TASK*Test"`.
