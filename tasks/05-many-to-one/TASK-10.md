# Задание 10. Откат транзакции: FK возвращается
- Тема: @ManyToOne (Product → Category)
- Difficulty: 10
- Что нужно сделать: Добавьте в `ProductLifecycleService` метод `void moveProductToCategory(Long productId, String categoryName)` — в транзакции (простой `@Transactional`, REQUIRED — вливается в транзакцию вызывающего кода) загрузить товар и перевести его в существующую категорию по имени. Заранее созданный тест (TASK10Test, `@SpringBootTest` + `TransactionTemplate`) вызывает ваш метод внутри внешней транзакции, которая завершается ОТКАТОМ, и доказывается: в новой транзакции товар снова в категории A — и по значению FK в БД, и по состоянию при свежей загрузке.
- Стартовый код:
```java
// com/example/jpalab/service/ProductLifecycleService.java

// TODO: @Transactional: find(product) -> find category by name -> setCategory
void moveProductToCategory(Long productId, String categoryName);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t05.TASK10Test#rollbackRestoresFk
```
- Критерий приёмки: Тест: после отката `category_id` в БД — id категории A (UPDATE не зафиксирован); свежая загрузка товара показывает категорию A; в SQL-логе откатившейся транзакции виден и `update products ... category_id`, и `rollback` (обновление выполнялось, но не пошло в БД); в комментарии зафиксировано: dirty checking и UPDATE — часть транзакции, откат отменяет и их.
- Подсказка (не решение): пока транзакция не закоммичена, «обновлённый» FK живёт только в БД-транзакции — rollback его стирает.
