# TASK-04. @DiscriminatorValue: явные значения дискриминатора

Тема: Entity Inheritance. Сложность: 4/10.

## Что делать

Зафиксировать значения колонки-дискриминатора `ptype`:

- `DigitalProduct` — `"DIGITAL"`;
- `PhysicalProduct` — `"PHYSICAL"`.

Артефакт — аннотации `@DiscriminatorValue` на подклассах
(настройка из TASK-03 сохраняется).

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t17.TASK04Test#explicitDiscriminatorValuesInColumn
```

## Критерии

- в `products_base` у строки цифрового товара `ptype = 'DIGITAL'`;
- у строки физического товара `ptype = 'PHYSICAL'`.

## Подсказка

Без `@DiscriminatorValue` Hibernate 6 пишет в колонку имя класса
(`DigitalProduct`/`PhysicalProduct`) — тест сравнивает точное
значение, поэтому неявный дискриминатор не пройдёт.
