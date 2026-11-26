# Задание 07. FK-ограничение на уровне БД
- Тема: @ManyToOne (Product → Category)
- Difficulty: 7
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `Long createCategory(String name)` — в своей транзакции сохранить категорию и вернуть её id. Заранее созданный тест (TASK07Test) доказывает, что внешний ключ гарантируется БД, а не только маппингом: ваш метод создаёт реальную категорию; затем native-INSERT строки в `products` со «выдуманным» `category_id` (такого id в `categories` нет) завершается нарушением целостности, а вставка с существующим id (вашим) проходит.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: categoryRepository.save(new Category(name)) -> return id
Long createCategory(String name);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK07Test#dbEnforcesFk
```
- Критерий приёмки: Тест: ваш метод возвращает ненулевой id; native INSERT с вымышленным `category_id` бросает `org.hibernate.exception.ConstraintViolationException` (Hibernate-обёртка над нарушением ссылочной целостности H2; при работе напрямую через `EntityManager` Spring не транслирует её в `DataIntegrityViolationException` — транслирует только на границе репозитория) — `assertThrows`; native INSERT с id из вашего метода проходит (строка создаётся, `created_at`/`updated_at` передаются явно); в комментарии записано имя/смысл ограничения (products.category_id → categories.id) и вывод: даже «правильный» JPA-маппинг нельзя обходить на уровне SQL без нарушения FK.
- Подсказка (не решение): DDL из `create-drop` содержит `foreign key ... references categories(id)` — БД сама не даст подставить чужой ключ.
