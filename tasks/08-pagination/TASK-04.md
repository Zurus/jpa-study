# Задание 04. Page + фильтрация
- Тема: Пагинация (Page / Slice / Pageable / Sort)
- Difficulty: 4
- Что нужно сделать: Добавьте в `ProductRepository` derived query с предикатом (товары дороже заданной цены) и параметром `Pageable`, возвращающий `Page<Product>`. Тест создан заранее (TASK04Test): 6 товаров с ценами 10..60, порог 20 — подходят 4; проверяется, что count считается по ОТФИЛЬТРОВАННОМУ множеству.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // метод должен иметь точно такую сигнатуру:
    Page<Product> findByPriceGreaterThan(BigDecimal minPrice, Pageable pageable);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t08.TASK04Test#pageCountReflectsPredicate
```
- Критерий приёмки: Тест: `getTotalElements() == 4` (count выполнен с тем же предикатом, что и данные — если бы count считал все 6, здесь бы было 6); товар ровно на границе (цена 20) в результат НЕ входит (предикат строгий); страница 0 — 3 элемента, вторая страница — 1; в SQL-логе count-запрос содержит условие цены.
- Подсказка (не решение): предикат + `Pageable` в одном derived-методе — Spring Data применит условие и к count, и к данным.
