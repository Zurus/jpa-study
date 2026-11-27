# TASK-03. REQUIRES_NEW: commit переживает rollback внешней

Тема: `@Transactional`: propagation и rollback. Сложность: 3/10.

## Что делать

Добавить метод в `com.example.jpalab.service.ProductService`:

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void createProductWithCategory(String productName, String categoryName) {
    // TODO
}
```

Создать новую `Category` с именем `categoryName` и новый `Product` с
именем `productName` (цена 0, склад 0, `product.category` = новая
категория) и сохранить их.

Требование: метод работает в СОБСТВЕННОЙ транзакции — его commit
происходит независимо от судьбы транзакции вызывающего.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t19.TASK03Test#requiresNewCommitsWhileOuterRollsBack
```

## Критерии

- тест открывает внешнюю транзакцию, persist-ит «пробный» товар,
  вызывает ваш метод, затем сам срывается (rollback внешней);
- после этого: пробного товара в БД нет, а созданные методом товар и
  категория на месте, товар ссылается на новую категорию;
- вызов без транзакции у вызывающего тоже должен работать
  (REQUIRES_NEW сам открывает).

## Подсказка

REQUIRES_NEW подвешивает текущую транзакцию (вызывающая — на паузе),
открывает новую с собственным соединением и собственной судьбой.
Сравните с заданием 05 — там тот же вызов из транзакции, но другой
уровень propagation.
