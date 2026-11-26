# Задание 06. Отрицание через not

- Тема: Specification: динамические предикаты
- Difficulty: 6
- Что нужно сделать: В `ProductSpecifications` добавьте статический метод
  `withoutNameContaining(String fragment)`, возвращающий `Specification<Product>`:
  товары, у которых `name` НЕ содержит `fragment` без учёта регистра.
- Стартовый код:
```java
public class ProductSpecifications {
    // TODO: отрицание предиката contains
    public static Specification<Product> withoutNameContaining(String fragment)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t13.TASK06Test#notExcludesMatchingNames
```
- Критерий приёмки: `withoutNameContaining("super")` возвращает только «Boring item»
  (оба «super...» исключены независимо от регистра); фрагмент, отсутствующий у всех
  (`"zzz"`), возвращает все 3 товара.
- Подсказка (не решение): `Predicate.not(Predicate)` или метод `Specification.not()` —
  отрицание готового предиката, а не написание «not-условия» заново.
