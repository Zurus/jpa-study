# TASK-03. SINGLE_TABLE: одна общая таблица с дискриминатором

Тема: Entity Inheritance. Сложность: 3/10.

## Что делать

Перевести иерархию `BaseEntity → DigitalProduct / PhysicalProduct`
на стратегию `SINGLE_TABLE`:

- `BaseEntity` становится **корнем иерархии-`@Entity`**
  (аннотацию `@MappedSuperclass` убрать);
- иерархия хранится в одной таблице `products_base`;
- тип подкласса определяется по колонке-дискриминатору `ptype`.

Артефакт — только аннотации на сущностях, методы не требуются.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t17.TASK03Test#singleTableOneSharedTableWithDiscriminator
```

## Критерии

- в `products_base` ровно 2 строки (цифровой + физический товар);
- у каждой строки `ptype` заполнен;
- у цифрового товара заполнена только `download_url`,
  у физического — только `weight`;
- таблицы `digital_product` и `physical_product` больше не существуют.

## Подсказка

На корневой сущности указываются стратегия, колонка-дискриминатор
и имя общей таблицы. В Hibernate 6 `@Inheritance` на
`@MappedSuperclass` игнорируется — корень обязан быть `@Entity`.
Значения дискриминатора пока не фиксируются (это TASK-04).
