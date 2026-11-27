# TASK-08. TABLE_PER_CLASS: иерархия Document / PdfDocument / OdtDocument

Тема: Entity Inheritance. Сложность: 8/10.

## Что делать

Создать отдельную иерархию со стратегией `TABLE_PER_CLASS`
(пакет `com.example.jpalab.entity.document`):

- `Document` — корень-`@Entity`: `id` (SEQUENCE — `IDENTITY`
  с этой стратегией несовместим), `createdAt: LocalDateTime`,
  `title: String`; `@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)`;
- `PdfDocument extends Document` — `pageCount: Integer`,
  `@Table(name = "pdf_documents")`;
- `OdtDocument extends Document` — `template: String`,
  `@Table(name = "odt_documents")`.

Только `lombok`-геттеры/сеттеры и конструктор по умолчанию.
Методы сервиса не требуются.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t17.TASK08Test#tablePerClassSeparateTablesFullColumns
```

## Критерии

- `PdfDocument` живёт только в `pdf_documents`, `OdtDocument` —
  только в `odt_documents`;
- каждая таблица содержит **все** колонки, включая унаследованные
  (`created_at`, `title`), и не содержит чужих;
- полиморфный запрос по `Document` — ровно **1** prepared
  statement (UNION по таблицам подклассов).

## Подсказка

`TABLE_PER_CLASS` не поддерживает `IDENTITY` (union-subclass):
используйте `@GeneratedValue(strategy = SEQUENCE, generator = ...)`
+ `@SequenceGenerator(allocationSize = 1)`. Дискриминатор здесь
не нужен — тип определяется таблицей.
