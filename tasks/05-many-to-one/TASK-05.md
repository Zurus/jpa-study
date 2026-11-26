# Задание 05. Identity: одна инстанция категории на два товара
- Тема: @ManyToOne (Product → Category)
- Difficulty: 5
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `Category loadCategoryOfProduct(Long productId)`: в своей транзакции загрузить товар по id и вернуть его категорию (`product.getCategory()` — LAZY-инициализация). Заранее созданный тест (TASK05Test): два товара в одной категории; категории у обоих товаров загружаются через ваш метод; работает identity-карта первого уровня кэша — у второго товара должна получиться ТОТ ЖЕ объект категории, что и у первого.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: find(product) -> return product.getCategory()
Category loadCategoryOfProduct(Long productId);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK05Test#sameCategoryInstance
```
- Критерий приёмки: Тест: до первого доступа категории у обоих товаров не инициализированы (прокси); ваш метод для первого товара инициализирует категорию; для второго — `isSameAs` с первым (не копия); всего ровно 3 prepared statement (2 по товарам + 1 по категории — повторного запроса к категории нет); в комментарии зафиксировано: key = (entity name, id) в persistence context.
- Подсказка (не решение): persistence context хранит инстанцию на (сущность, id); второй запрос по той же сущности вернёт уже загруженный объект.
