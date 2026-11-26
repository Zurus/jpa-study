# Задание 06. Dirty checking: FK и поле в одном UPDATE
- Тема: @ManyToOne (Product → Category)
- Difficulty: 6
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `void updatePriceAndCategory(Long productId, BigDecimal newPrice, String categoryName)`: в своей транзакции загрузить товар, за одно «испортить» сущность сразу в двух местах — `setPrice(newPrice)` и перевести товар в существующую категорию по имени. Один flush — Hibernate должен сгенерировать ОДИН UPDATE с обеими колонками.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: find(product) -> setPrice(newPrice); setCategory(byName) 
void updatePriceAndCategory(Long productId, BigDecimal newPrice, String categoryName);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK06Test#singleUpdateTwoColumns
```
- Критерий приёмки: Тест: данные — товар в категории A (цена 100.00), категория B; после вызова `entityUpdateCount` == 1 (ровно один `update products`); в БД цена 150.00 и `category_id` = id(B); в комментарии зафиксировано, что dirty checking сравнивает снапшот (snapshot) с текущим состоянием и строит один UPDATE на сущность, а не по UPDATE на поле.
- Подсказка (не решение): один managed-объект = один UPDATE в flush, какие бы поля ни «пачкались» (включая FK-колонку).
