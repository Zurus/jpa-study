# Задание 01. Первый Specification: точное совпадение по имени

- Тема: Specification: динамические предикаты
- Difficulty: 1
- Что нужно сделать: Создайте класс `com.example.jpalab.repository.ProductSpecifications`
  (публичный, публичный конструктор без параметров) со статическим методом
  `byName(String name)`, возвращающим `Specification<Product>`: предикат точного
  совпадения поля `name`. `JpaSpecificationExecutor` уже добавлен в
  `ProductRepository` — результат проверяется `productRepository.findAll(spec)`.
- Стартовый код:
```java
// com/example/jpalab/repository/ProductSpecifications.java
public class ProductSpecifications {
    // TODO: предикат eq на name
    public static Specification<Product> byName(String name)
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t13.TASK01Test#byNameFindsExactMatch
```
- Критерий приёмки: `byName("Widget B")` находит ровно один товар;
  `byName("Widget")` (короткое имя) ничего не находит — совпадение точное, не «contains».
- Подсказка (не решение): один атомарный предикат сравнения на `root.get("name")` через `CriteriaBuilder`.
