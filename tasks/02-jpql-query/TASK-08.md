# Задание 08. Ловушка: свойство сущности vs колонка БД
- Тема: JPQL @Query
- Difficulty: 8
- Что нужно сделать: Тест создан заранее (TASK08Test): он строит запросы динамически через `EntityManager`, не добавляя битых методов в репозиторий (контекст не поднимется): (а) JPQL-запрос, где условие использует имя КОЛОНКИ `p.stock_quantity`; (б) тот же запрос с именем СВОЙСТВА `p.stockQuantity`. Сравните поведение и зафиксируйте его в тесте.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t02.TASK08Test
// Он использует EntityManager:
//   (a) em.createQuery("SELECT p FROM Product p WHERE p.stock_quantity > :s", Product.class)
//   (b) em.createQuery("SELECT p FROM Product p WHERE p.stockQuantity > :s", Product.class)
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t02.TASK08Test#columnVsProperty
```
- Критерий приёмки: Тест: вариант (a) бросает `IllegalArgumentException` (обёртка `SemanticException`: путь не распознан) — проверяется `assertThrows`; вариант (b) возвращает ожидаемые товары; в комментарии к тесту зафиксировано: JPQL — язык модели сущностей, имена колонок существуют только в DDL и native-запросах, поэтому метод в репозитории с «колонкой» убил бы запуск приложения.
- Подсказка (не решение): в JPQL пишите `camelCase`-свойства; `snake_case`-колонки Hibernate подставит сам.
