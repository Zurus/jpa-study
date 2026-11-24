# Задание 10. Slice + фильтр + Sort: механика «лишней» строки
- Тема: Пагинация (Page / Slice / Pageable / Sort)
- Difficulty: 10
- Что нужно сделать: Добавьте в `ProductRepository` derived-метод, возвращающий `Slice<Product>` с предикатом (имя содержит фрагмент, без учёта регистра) и `Pageable`. Тест создан заранее (TASK10Test): 3 подходящих товара, `size = 2` — проверено, как «лишняя» строка определяет `hasNext` в двух крайних случаях: когда строк больше страницы и когда после смещения остаётся ровно одна.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // метод должен иметь точно такую сигнатуру:
    Slice<Product> sliceByNameContainingIgnoreCase(String fragment, Pageable pageable);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t08.TASK10Test#sliceExtraRowDrivesHasNext
```
- Критерий приёмки: Тест: s0 — 2 элемента и `hasNext == true`; s1 (offset 2) — 1 элемент и `hasNext == false`; на каждый вызов ровно 1 prepared statement (count-запроса нет), в SQL-логе `fetch first ? rows only`; сортировка из `Pageable` соблюдена в контенте; в комментарии зафиксирована механика: драйверу запрашивается `size + 1` строк — «лишняя» строка лишь сигнал «есть ещё», в контент она не попадает.
- Подсказка (не решение): `hasNext` у `Slice` — это «дотянулась ли (size+1)-я строка», а не знание общего количества.
