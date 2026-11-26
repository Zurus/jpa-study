# Задание 08. Нельзя удалить «одного», пока на него ссылаются
- Тема: @ManyToOne (Product → Category)
- Difficulty: 8
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `void deleteCategoryWithProducts(Long categoryId)` — в ОДНОЙ СВОЕЙ транзакции удалить все товары категории, затем саму категорию (правильный порядок удаления). Заранее созданный тест (TASK08Test): категория с 2 товарами; прямое удаление категории через `categoryRepository.delete` при живых товарах фиксирует нарушение FK (категория остаётся в БД); затем ваш метод удаляет категорию с товарами — обе таблицы пусты.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: в одной транзакции: удалить все товары категории -> удалить категорию
void deleteCategoryWithProducts(Long categoryId);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK08Test#cannotDeleteReferencedCategory
```
- Критерий приёмки: Тест: прямое удаление категории при живых товарах — `org.hibernate.exception.ConstraintViolationException` (Hibernate-обёртка над нарушением FK H2; при работе напрямую через `EntityManager` Spring не транслирует её в `DataIntegrityViolationException`) — `assertThrows`; категория остаётся в БД (native `SELECT COUNT(*)` = 1); после вызова вашего метода: `COUNT(*) FROM categories WHERE id = ?` = 0 и `COUNT(*) FROM products` = 0; в комментарии зафиксирован вывод: «одна» сторона ManyToOne защищена ссылками «многих», и порядок удаления — сначала «много», потом «один» (или каскад со стороны коллекции из темы 6).
- Подсказка (не решение): FK — это правило БД: строка, на которую указывают другие строки, не удаляется.
