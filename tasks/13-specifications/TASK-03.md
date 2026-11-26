# Задание 03. Динамический фильтр: null-фрагмент

- Тема: Specification: динамические предикаты
- Difficulty: 3
- Что нужно сделать: В `ProductSpecifications` добавьте статический метод
  `byNameContainingIgnoreCaseOptional(String fragment)`, возвращающий
  `Specification<Product>`: если `fragment == null` — предикат «всегда верен»
  (возвращаются все строки); иначе — `name` содержит фрагмент без учёта регистра.
- Стартовый код:
```java
public class ProductSpecifications {
    // TODO: null -> «всегда верен», иначе case-insensitive contains
    public static Specification<Product> byNameContainingIgnoreCaseOptional(String fragment)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t13.TASK03Test#nullFragmentMatchesEverything
```
- Критерий приёмки: `null` возвращает все 3 товара; `"WIDG"` — только «Super Widget»;
  `"SUPER"` — и «Super Widget», и «super gizmo» (регистр не важен).
- Подсказка (не решение): «выключенный» фильтр — предикат-константа (например, `true`);
  нормализацию регистра применяйте к полю, а не к параметру.
