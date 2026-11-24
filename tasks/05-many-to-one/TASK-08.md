# Задание 08. Нельзя удалить «одного», пока на него ссылаются
- Тема: @ManyToOne (Product → Category)
- Difficulty: 8
- Что нужно сделать: Тест создан заранее (TASK08Test): категория с 2 товарами. Попытка удалить саму категорию (`categoryRepository.delete`) фиксирует ошибку, вызванную тем, что на категорию ссылаются `products.category_id`. Затем показан «правильный порядок»: сначала удалить товары, потом категорию.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t05.TASK08Test
// assertThrows для delete категории при живых товарах (DataIntegrityViolationException);
// категория остаётся в БД (native SELECT);
// затем: deleteAll товаров -> delete категории -> ок
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK08Test#cannotDeleteReferencedCategory
```
- Критерий приёмки: Тест: удаление категории при живых товарах завершается `org.hibernate.exception.ConstraintViolationException` (Hibernate-обёртка над нарушением FK H2; при работе напрямую через `EntityManager` Spring не транслирует её в `DataIntegrityViolationException`) — `assertThrows`; категория остаётся в БД (проверить native SELECT); после удаления товаров удаление категории проходит без ошибки; в комментарии зафиксирован вывод: «одна» сторона ManyToOne защищена ссылками «многих», и порядок удаления — сначала «много», потом «один» (или `orphanRemoval`/каскод со стороны коллекции из темы 6).
- Подсказка (не решение): FK — это правило БД: строка, на которую указывают другие строки, не удаляется.
