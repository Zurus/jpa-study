# Задание 07. LEFT JOIN в проекции: товары без категории
- Тема: Проекции
- Difficulty: 7
- Что нужно сделать: (1) Создайте интерфейс-проекцию `ProductNameCategory` в пакете `com.example.jpalab.dto` с getter'ами `String getProductName()`, `String getCategoryName()`. (2) В `ProductRepository` добавьте БЕЗПАРАМЕТРНЫЙ JPQL-метод `List<ProductNameCategory> namesWithOptionalCategory()`: имя товара + имя категории (или null) для ВСЕХ товаров, включая те, у которых категории нет. Сортировка — по имени товара по возрастанию.
- Стартовый код:
```java
// src/main/java/com/example/jpalab/dto/ProductNameCategory.java (новый файл)

public interface ProductNameCategory {
    String getProductName();
    String getCategoryName();
}
```
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Query — LEFT JOIN p.category, SELECT имя товара, имя категории,
//       ORDER BY p.name ASC
List<ProductNameCategory> namesWithOptionalCategory();
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t10.TASK07Test#leftJoinProjectionKeepsNullCategory
```
- Критерий приёмки: Тест: P1, P2 — с категорией WithCat, P3 — без; выборка содержит ВСЕ ТРИ товара (с INNER JOIN P3 молча выпадает и тест не пройдёт); у P3 getCategoryName() = null.
- Подсказка (не решение): разница INNER/LEFT JOIN в JPQL — в одном слове; «опциональная» сторона должна стоять справа от JOIN.
