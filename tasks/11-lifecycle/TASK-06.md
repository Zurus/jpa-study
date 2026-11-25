# Задание 06. Identity map в одной транзакции: одна инстанция
- Тема: Жизненный цикл Entity
- Difficulty: 6
- Что нужно сделать: Реализуйте в `ProductLifecycleService` метод, который в **одной** REQUIRES_NEW-транзакции загружает товар по id сначала через `em.find`, затем через `productRepository.findById`, и возвращает результат сравнения двух инстанций оператором `==`. Наблюдаем: primary key → одна инстанция внутри одного persistence context (identity map / первый уровень кэша); `EntityManager` и репозиторий делят один PC.
- Стартовый код:
```java
// TODO: добавьте метод в существующий com.example.jpalab.service.ProductLifecycleService (@Service)
public boolean sameInstanceForEntityManagerAndRepository(Long id) {
    // TODO: одна REQUIRES_NEW-транзакция: a = em.find(...); b = repo.findById(...).get();
    // TODO: вернуть (a == b)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t11.TASK06Test#identityMapReturnsSameInstanceInOneTransaction
```
- Критерий приёмки: Тест: `a == b` — true. В SQL-логе за одну транзакцию один `select ... from products where id = ?` (второй find/нашёл инстанцию в identity map и не ходил в БД).
- Подсказка (не решение): identity map живёт в `Session` (persistence context) — ключ сравнения инстанций — ссылка (`==`), а не `equals`.
