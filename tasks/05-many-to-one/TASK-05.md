# Задание 05. Identity: одна инстанция категории на два товара
- Тема: @ManyToOne (Product → Category)
- Difficulty: 5
- Что нужно сделать: Тест создан заранее (TASK05Test): два товара в одной категории. Загружаются оба товара из БД (свежий persistence context, категории — прокси), категория инициализируется через ПЕРВЫЙ товар, затем сравниваются инстанции у ВТОРОГО. Работает identity-карта первого уровня кэша.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t05.TASK05Test
// 2 товара, 1 категория; flush + clear; p1, p2 = findById
// c1 = p1.getCategory() (инициализация); c2 = p2.getCategory()
// assertThat(c2).isSameAs(c1); на категории ушёл ровно 1 SELECT
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK05Test#sameCategoryInstance
```
- Критерий приёмки: Тест: до первого доступа `Hibernate.isInitialized(p2.getCategory()) == false` (прокси); после инициализации через p1 — `p2.getCategory()` — ТОТ ЖЕ объект (`isSameAs`), а не копия; в SQL-логе на загрузку категорий ушёл ровно 1 SELECT (второй товар не заставлял БД повторять запрос); в комментарии зафиксировано: key = (entity name, id) в persistence context.
- Подсказка (не решение): persistence context хранит инстанцию на (сущность, id); второй запрос по той же сущности вернёт уже загруженный объект.
