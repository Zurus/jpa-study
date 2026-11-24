# Задание 01. Первый ManyToOne: FK null и FK установлен
- Тема: @ManyToOne (Product → Category)
- Difficulty: 1
- Что нужно сделать: Тест создан заранее (TASK01Test): создайте товар БЕЗ категории и сохраните его — убедитесь, что `category_id` в БД равен NULL. Затем привяжите товар к категории (setCategory) и сохраните ещё раз — убедитесь, что `category_id` установлен. Реальное значение колонки проверяется через native-запрос.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t05.TASK01Test
// 1) сохранение товара без категории -> SELECT category_id -> NULL
// 2) product.setCategory(category); save
// 3) тот же SELECT -> id категории
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK01Test#fkLifecycle
```
- Критерий приёмки: Тест: после первого сохранения `SELECT category_id FROM products WHERE id = ?` возвращает NULL; после привязки — id категории; в SQL-логе между шагами 1 и 2 виден `update products` с колонкой `category_id` (или insert с FK, если вы пересоздали).
- Подсказка (не решение): ManyToOne — «владелец» связи: колонку-ключ он заполняет сам при сохранении/обновлении.
