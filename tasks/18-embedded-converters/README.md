# Тема 18. Embedded-типы и конвертеры

Ценностные типы и кастомный маппинг: `@Embeddable`/`@Embedded`,
переопределение колонок вложенных типов, маппинг enum
(`@Enumerated`, `AttributeConverter`) и сохранение сложных значений
в одну колонку.

## Что в теме

- `@Embeddable` + `@Embedded`: ценностный тип `Money` в таблице
  сущности `Offer`;
- дефолтные имена колонок вложенного типа и их переопределение
  (`@AttributeOverrides`/`@AttributeOverride`);
- два вложенных типа одного класса в одной сущности (конфликт
  колонок и его решение);
- `@Enumerated(EnumType.STRING)` / `@Enumerated(EnumType.ORDINAL)`
  — что именно попадает в колонку;
- `AttributeConverter` + явный `@Convert`: кастомное
  представление enum в БД только для помеченного поля;
- `@Converter(autoApply = true)`: конвертер для всех полей типа
  без аннотации на поле;
- конвертер сложного типа: `Money` в одной текстовой колонке;
- вложенный `@Embeddable` (embeddable внутри embeddable) с
  `@AttributeOverrides` внутри вложенного класса;
- агрегация JPQL по пути вложенного типа (NULL-семантика SUM).

## Стартовый код (уже в проекте)

- `com.example.jpalab.entity.Money` — `@Embeddable`:
  `amount` (`BigDecimal`), `currency` (`String`);
- `com.example.jpalab.entity.ProductStatus` — enum
  `ACTIVE` / `INACTIVE` / `DISCONTINUED`;
- `com.example.jpalab.entity.DiscountType` — enum `PERCENT` / `FIXED`;
- `com.example.jpalab.entity.Offer` — `@Entity` (таблица `offer`):
  `id`, `name`, `basePrice` (`@Embedded Money`, колонки по
  умолчанию `amount`/`currency`), `status`
  (`@Enumerated(EnumType.STRING)`), `discountType` (без аннотаций);
- поле `status: ProductStatus` добавлено в `Product`
  (`@Enumerated(EnumType.STRING)`) — контроль области действия
  конвертеров в задании 05;
- сервис-скелет `com.example.jpalab.service.OfferService`
  (только `EntityManager`, методов нет — их добавляют вы);
- пустой `com.example.jpalab.repository.OfferRepository`.

Конвертеры и новый `@Embeddable` `PriceRange` создаёт студент
(задания 05–08).

## Границы темы

- Только `@Embeddable`, `@Embedded`, `@AttributeOverrides` /
  `@AttributeOverride`, `@Enumerated`, `AttributeConverter`,
  `@Convert`, `@Converter`.
- Без `@ElementCollection` (тема 15) и `@ManyToMany` (тема 16).
- Без проекций и DTO (тема 10): задания возвращают сущности,
  скаляры и `BigDecimal`-суммы.
- Без аудита (тема 12) и жизненного цикла (тема 11).
- Без составных ключей (`@EmbeddedId`), `@SecondaryTable`,
  наследования (тема 17).
- Задания 02–09 — накопительные: аннотации и поля из предыдущих
  заданий остаются на месте, тесты всех пройденных заданий
  остаются зелёными.

## Задания

| # | Файл | Артефакт | Сложность |
|---|------|----------|-----------|
| 01 | [TASK-01.md](TASK-01.md) | `saveOffer(String, Money, ProductStatus)` | 1 |
| 02 | [TASK-02.md](TASK-02.md) | `@AttributeOverrides` для `basePrice` | 2 |
| 03 | [TASK-03.md](TASK-03.md) | второй `@Embedded Money` + свои колонки | 3 |
| 04 | [TASK-04.md](TASK-04.md) | `@Enumerated(EnumType.ORDINAL)` для `lastKnownStatus` | 4 |
| 05 | [TASK-05.md](TASK-05.md) | `ProductStatusConverter` + явный `@Convert` | 5 |
| 06 | [TASK-06.md](TASK-06.md) | `DiscountTypeConverter`, `@Converter(autoApply = true)` | 6 |
| 07 | [TASK-07.md](TASK-07.md) | `MoneyTextConverter`: `Money` в одну колонку | 7 |
| 08 | [TASK-08.md](TASK-08.md) | `@Embeddable PriceRange` (вложенный) + overrides | 8 |
| 09 | [TASK-09.md](TASK-09.md) | `@AttributeOverride` с атрибутами `@Column` | 9 |
| 10 | [TASK-10.md](TASK-10.md) | `sumBaseAmountByCurrency(String)` (JPQL по пути вложенного) | 10 |

Проверка каждого задания — точная команда в его файле.

Вся тема: `mvn -Dtest="com.example.jpalab.tasks.t18.TASK*Test"`.
