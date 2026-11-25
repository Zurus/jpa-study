# Задание 07. Identity map в разных транзакциях: разные инстанции
- Тема: Жизненный цикл Entity
- Difficulty: 7
- Что нужно сделать: Реализуйте в `ProductLifecycleService` метод, который в **первой** REQUIRES_NEW-транзакции загружает товар по id (`em.find`), сохраняет инстанцию в локальную переменную, затем в **второй** REQUIRES_NEW-транзакции загружает товар по тому же id и возвращает результат сравнения двух инстанций (`==`). Наблюдаем: identity map — свойство persistence context, то есть транзакции; «глобального» кэша инстанции в JPA нет — каждая транзакция строит свою.
- Стартовый код:
```java
// TODO: добавьте метод в существующий com.example.jpalab.service.ProductLifecycleService (@Service)
public boolean sameInstanceAcrossTransactions(Long id) {
    // TODO: первая REQUIRES_NEW-транзакция: a = em.find(...);
    // TODO: вторая REQUIRES_NEW-транзакция: b = em.find(...);
    // TODO: вернуть (a == b)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t11.TASK07Test#differentTransactionsDifferentInstances
```
- Критерий приёмки: Тест: `a == b` — false (две разные инстанции с одинаковыми данными). Вместе с заданием 06 фиксируется пара выводов: одна транзакция — одна инстанция; разные транзакции — разные инстанции.
- Подсказка (не решение): после commit первой транзакции её инстанция — detached; второй `em.find` создаёт новую инстанцию во втором PC.
