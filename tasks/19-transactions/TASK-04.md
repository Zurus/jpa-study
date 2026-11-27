# TASK-04. MANDATORY: только внутри транзакции вызывающего

Тема: `@Transactional`: propagation и rollback. Сложность: 4/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductService`:

```java
@Transactional(propagation = Propagation.MANDATORY)
public void moveProductToCategory(Long productId, String categoryName) {
    // TODO
}
```

Установить товару `productId` категорию с именем `categoryName`
(категория уже существует в БД — найти её по имени, например, JPQL).

Требование: метод обязан выполняться ТОЛЬКО внутри уже открытой
транзакции вызывающего. При вызове без транзакции тело не выполняется
вообще — прокси бросает `IllegalTransactionStateException`.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t19.TASK04Test#mandatoryRequiresCallerTransaction
```

## Критерии

- прямой вызов (без транзакции): бросается
  `IllegalTransactionStateException`; в БД ничего не изменилось —
  категория не создана, `category_id` товара по-прежнему NULL;
- вызов внутри `TransactionTemplate`: категория установлена,
  изменение закоммичено.

## Подсказка

MANDATORY — «транзакция обязательна, свою не создавать». Это способ
задокументировать и гарантировать контракт «метод — часть транзакции
вызывающего», а не «метод сам по себе транзакция».
