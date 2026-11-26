# Задание 05. Проекция с JOIN: товар + имя категории (новый интерфейс)
- Тема: Проекции
- Difficulty: 5
- Что нужно сделать: (1) Создайте открытый интерфейс-проекцию `ProductCategoryView` в пакете `com.example.jpalab.dto` с getter'ами `String getProductName()`, `String getCategoryName()`, `BigDecimal getPrice()`. (2) В `ProductRepository` добавьте JPQL-метод `List<ProductCategoryView> withCategoryName(@Param("categoryName") String categoryName)` с JOIN по категории: возвращает имя товара, ИМЯ категории и цену товаров той категории, имя которой равно `:categoryName`. Сортировка: цена по убыванию, тай-брейк — имя товара по возрастанию.
- Стартовый код:
```java
// src/main/java/com/example/jpalab/dto/ProductCategoryView.java (новый файл)

public interface ProductCategoryView {
    String getProductName();
    String getCategoryName();
    BigDecimal getPrice();
}
```
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Query — JOIN p.category, c.name = :categoryName,
//       SELECT: имя товара, имя категории, цена; ORDER BY price DESC, name ASC
List<ProductCategoryView> withCategoryName(@Param("categoryName") String categoryName);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t10.TASK05Test#projectionWithJoinToCategory
```
- Критерий приёмки: Тест: категории Audio (Headphones 300.00, Mic 100.00) и Home (Speaker 200.00); вызов с "Audio" — ровно 2 строки: Headphones/Mic, обе с getCategoryName() = "Audio", Speaker отсутствует.
- Подсказка (не решение): для open interface каждый столбец SELECT получает алиас, совпадающий с именем свойства (productName, categoryName, price); INNER JOIN по категории сам исключает товары без категории.
