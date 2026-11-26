# 15. Элементные коллекции (@ElementCollection)

10 заданий с нарастающей сложностью (сложность 1–10).

## Что используется
- `Product.tags: Set<String>` через `@ElementCollection` (таблица `product_tags`).
- `Product.attributes: Map<String, String>` через `@ElementCollection` + `@MapKeyColumn` (таблица `product_attributes`).
- `EntityManager` (загрузка owner, изменение управляемых коллекций, JPQL/HQL).

## Что не используется (границы темы)
- `@OneToMany` и `@ManyToMany` (элементы коллекций — не сущности).
- Отдельные сущности для элементов коллекций.
- Повтор тем 05–07 (ассоциации) и 16 (продвинутый маппинг типов).

## Методы-артефакты студента
Все методы — в `com.example.jpalab.service.ProductElementCollectionService`
(стартовый код: `@Service` + `@PersistenceContext EntityManager`, методов нет).

| # | Метод | Сложность |
|---|-------|-----------|
| 01 | `void addTag(Long productId, String tag)` | 1 |
| 02 | `void removeTag(Long productId, String tag)` | 2 |
| 03 | `void setAttribute(Long productId, String key, String value)` | 3 |
| 04 | `void removeAttribute(Long productId, String key)` | 4 |
| 05 | `Set<Long> findProductIdsByTag(String tag)` | 5 |
| 06 | `Set<Long> findProductIdsByAttribute(String key, String value)` | 6 |
| 07 | `Set<Long> findProductIdsWithAnyTag(List<String> tags)` | 7 |
| 08 | `void syncTags(Long productId, Set<String> newTags)` | 8 |
| 09 | `void updateAttributes(Long productId, Map<String, String> updates)` | 9 |
| 10 | `void persistProductWithPreparedCollection(Product product)` | 10 |

## Проверка
- Задание: `mvn -Dtest=<полное имя тестового класса>#<имя метода>` (точно — в каждом `TASK-XX.md`).
- Все 10 заданий: `mvn test` (тема 15 добавляет 10 тестов к общему набору).
- Все тесты темы должны быть красными до реализации и зелёными после.
- Тесты проверяют реальное состояние БД (таблицы `product_tags`, `product_attributes`)
  нативными SELECT — не «через память».

## Файлы
- Стартовый код сущности: `src/main/java/com/example/jpalab/entity/Product.java`
  (поля `tags` и `attributes` уже объявлены).
- Сервис-заготовка: `src/main/java/com/example/jpalab/service/ProductElementCollectionService.java`.
- Тесты: `src/test/java/com/example/jpalab/tasks/15-element-collection/TASK01Test.java` … `TASK10Test.java`
  (пакет `com.example.jpalab.tasks.t15`).
