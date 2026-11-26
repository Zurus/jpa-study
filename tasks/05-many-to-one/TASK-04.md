# Задание 04. Transient-категория: исключение без cascade
- Тема: @ManyToOne (Product → Category)
- Difficulty: 4
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `void saveProductWithTransientCategory(String productName, BigDecimal price, int stock, String newCategoryName)` — в СВОЕЙ транзакции создать НОВЫЙ (transient) товар и НОВУЮ (transient) категорию, привязать товар к категории и сохранить товар (`saveAndFlush`). Заранее созданный тест (TASK04Test, `@SpringBootTest` + `TransactionTemplate`) вызывает ваш метод в откатываемой транзакции и фиксирует: сохранение завершается исключением (тип — `InvalidDataAccessApiUsageException`, причина — `TransientPropertyValueException`), а в закоммиченной БД строки товара нет.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: new Product(...) + new Category(...) (БЕЗ сохранения категории) ->
//       product.setCategory(category) -> saveAndFlush(product)
void saveProductWithTransientCategory(String productName, BigDecimal price,
                                      int stock, String newCategoryName);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK04Test#transientCategoryRejected
```
- Критерий приёмки: Тест: ваш метод бросает `org.springframework.dao.InvalidDataAccessApiUsageException` с корневой причиной `org.hibernate.TransientPropertyValueException` («object references an unsaved transient instance») — `assertThrows`; в закоммиченной БД (новая tx) `SELECT COUNT(*) FROM products` = 0; в комментарии к тесту записано, почему (нет `cascade = PERSIST` на стороне ManyToOne), как это исправить в реальном приложении (сохранить категорию отдельно или добавить cascade — с указанием рисков) и почему при IDENTITY проверка «нет строки» возможна только после отката (IDENTITY делает INSERT в момент persist).
- Подсказка (не решение): Hibernate не может угадать, как сохранить «чужую» сущность: без cascade — ошибка, а не молчаливое persist.
