# TASK-07. TransactionTemplate: commit до срыва следующего шага

Тема: `@Transactional`: propagation и rollback. Сложность: 7/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductService`:

```java
public void transferStockCommittedFirst(Long fromId, Long toId, int amount) {
    // TODO
}
```

Поведение: БЕЗ `@Transactional` на самом методе выполнить перенос
`amount` со склада источника на склад получателя (снять, прибавить,
`flush`) в собственной программной транзакции через
`TransactionTemplate` — шаблон создаётся самим сервисом из
`PlatformTransactionManager` (добавьте его в конструктор). После commit
шаблона метод выбрасывает `IllegalStateException` (имитация срыва
следующего системного шага, например, отправки уведомления).

Требование: к моменту исключения перенос уже закоммичен — откатывать
нечего.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t19.TASK07Test#programmaticTemplateCommitsBeforeFailure
```

## Критерии

- склад 10/5, перенос 3;
- метод бросает `IllegalStateException`;
- в БД после исключения: 7 и 8 — перенос закоммичен ДО срыва;
- если бы перенос был внутри одной объявляющей транзакции метода,
  `IllegalStateException` откатил бы и его.

## Подсказка

`TransactionTemplate` — та же транзакция, только граница открыта и
закрыта явным кодом, а не аннотацией: всё, что выполнено до
`execute*`, закоммичено и невосстановимо. Это «ручное управление
границами» там, где декларативной границы слишком много или мало.
