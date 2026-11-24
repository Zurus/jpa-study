# Задание 04. Transient-категория: исключение без cascade
- Тема: @ManyToOne (Product → Category)
- Difficulty: 4
- Что нужно сделать: Тест, доказывающий, что по умолчанию cascade на ManyToOne НЕ настроен, создан заранее (TASK04Test, `@SpringBootTest` + `TransactionTemplate` — чтобы проверить состояние ЗАКОММИЧЕННОЙ БД в новой транзакции): создание товара с НОВОЙ (transient) категорией и попытка его сохранить завершается исключением; зафиксированы тип исключения и состояние БД (в закоммиченной БД строки товара нет). Фактическое поведение (Hibernate 6 + IDENTITY): `persist` проходит (IDENTITY сразу выполняет INSERT со `category_id = NULL` внутри транзакции), `flush` бросает исключение с причиной `org.hibernate.TransientPropertyValueException`; на границе репозитория Spring транслирует его в `org.springframework.dao.InvalidDataAccessApiUsageException` (при прямой работе через `EntityManager` — `IllegalStateException`); транзакция откатывается.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t05.TASK04Test
// @SpringBootTest + TransactionTemplate (свои setup/commit/очистка)
// Category fresh — без save; product.setCategory(fresh);
// tx.executeWithoutResult(saveAndFlush) -> assertThrows:
//   InvalidDataAccessApiUsageException (граница репозитория),
//   причина TransientPropertyValueException;
// новая tx: SELECT COUNT(*) FROM products -> 0
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK04Test#transientCategoryRejected
```
- Критерий приёмки: Тест: `saveAndFlush` в откатываемой транзакции бросает `org.springframework.dao.InvalidDataAccessApiUsageException` (на границе репозитория; причина — `org.hibernate.TransientPropertyValueException` «object references an unsaved transient instance») — `assertThrows`; в закоммиченной БД (новая tx) в `products` нет строки товара; в комментарии к тесту записано, почему (нет `cascade = PERSIST` на стороне ManyToOne), как это исправить в реальном приложении (сохранить категорию отдельно или добавить cascade — с указанием рисков) и почему при IDENTITY проверка «нет строки» возможна только после отката (IDENTITY делает INSERT в момент persist).
- Подсказка (не решение): Hibernate не может угадать, как сохранить «чужую» сущность: без cascade — ошибка, а не молчаливое persist.
