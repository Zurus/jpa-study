# Задание 07. FK-ограничение на уровне БД
- Тема: @ManyToOne (Product → Category)
- Difficulty: 7
- Что нужно сделать: Тест, доказывающий, что внешний ключ гарантируется БД, а не только маппингом, создан заранее (TASK07Test): вставится через native-INSERT строка в `products` со «выдуманным» `category_id` (такого id в `categories` нет) — зафиксируется нарушение целостности; затем вставка с существующим id пройдёт.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t05.TASK07Test
// save категории -> реальный id;
// native INSERT с несуществующим category_id -> assertThrows(DataIntegrityViolationException);
// повтор с реальным id -> ок (created_at/updated_at передаются явно)
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK07Test#dbEnforcesFk
```
- Критерий приёмки: Тест: INSERT с вымышленным `category_id` бросает `org.hibernate.exception.ConstraintViolationException` (Hibernate-обёртка над нарушением ссылочной целостности H2; при работе напрямую через `EntityManager` Spring не транслирует её в `DataIntegrityViolationException` — транслирует только на границе репозитория) — `assertThrows`; INSERT с существующим id проходит; в комментарии записано имя/смысл ограничения (products.category_id → categories.id) и вывод: даже «правильный» JPA-маппинг нельзя обходить на уровне SQL без нарушения FK.
- Подсказка (не решение): DDL из `create-drop` содержит `foreign key ... references categories(id)` — БД сама не даст подставить чужой ключ.
