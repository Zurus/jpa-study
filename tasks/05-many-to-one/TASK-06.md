# Задание 06. Dirty checking: FK и поле в одном UPDATE
- Тема: @ManyToOne (Product → Category)
- Difficulty: 6
- Что нужно сделать: Тест создан заранее (TASK06Test): managed-товар с категорией A. За одно изменение сущность «испорчена» сразу в двух местах: поменяйте `price` и переведите товар в категорию B. Один flush — и проверяется, что Hibernate сгенерировал ОДИН UPDATE с обеими колонками.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t05.TASK06Test
// product (A, цена 100); setPrice(150); setCategory(B); em.flush();
// через Hibernate Statistics: ровно один UPDATE сущности
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK06Test#singleUpdateTwoColumns
```
- Критерий приёмки: Тест: в SQL-логе между flush-ом и концом — ровно один `update products`; в нём присутствуют И `price`, И `category_id` (и `version`); значения в БД: новая цена и новый FK; в комментарии зафиксировано, что dirty checking сравнивает снапшот (snapshot) с текущим состоянием и строит один UPDATE на сущность, а не по UPDATE на поле.
- Подсказка (не решение): один managed-объект = один UPDATE в flush, какие бы поля ни «пачкались» (включая FK-колонку).
