# Задание 02. Смена FK: привязка товара к другой категории
- Тема: @ManyToOne (Product → Category)
- Difficulty: 2
- Что нужно сделать: Тест создан заранее (TASK02Test): товар привязан к категории A. Замените категорию на B одним вызовом `setCategory` и дождитесь flush. Убедитесь, что в БД обновился внешний ключ, а строка товара не пересоздавалась (тот же id).
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t05.TASK02Test
// product (категория A, id = ?); product.setCategory(B); em.flush();
// SELECT category_id FROM products WHERE id = ? -> id(B); id товара тот же
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK02Test#reassignCategory
```
- Критерий приёмки: Тест: после flush `category_id` равен id категории B; id товара не изменился; в SQL-логе ровно один `update products ... set ... category_id = ? ... where id = ?` (без delete/insert); в памяти `product.getCategory()` — категория B.
- Подсказка (не решение): изменение навигационного поля managed-сущности — это «грязь», которую dirty checking переведёт в UPDATE.
