# Задание 09. Merge отстёгнутого товара со сменой категории
- Тема: @ManyToOne (Product → Category)
- Difficulty: 9
- Что нужно сделать: Тест с «отстёгиванием» создан заранее (TASK09Test): загружается товар (категория A), persistence context очищается, у detached-копии меняется категория на B и выполняется `merge`. Проверяется, что FK обновился, версия увеличилась, а merge вернул managed-копию (не ту, что передали).
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t05.TASK09Test
// load product (A), em.clear(); detached.setCategory(B);
// managed = productRepository.save(detached) (merge); em.flush();
// managed isNotSameAs detached, version +1, SELECT category_id -> B,
// одна строка товара
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK09Test#mergeDetachedWithNewCategory
```
- Критерий приёмки: Тест: результат `save` (merge) — другая инстанция, чем переданная (isNotSameAs); `version` увеличен на 1; в БД `category_id = B` и одна строка товара (не две); в SQL-логе `update products` с `category_id` и `where id = ? and version = ?`; в комментарии зафиксировано: merge копирует состояние (включая FK) в managed-инстанцию — «отстёгнутый» объект по-прежнему detached.
- Подсказка (не решение): merge — это «применить diff отстёгнутого к управляемому»: навигационные поля копируются вместе со скалярами.
