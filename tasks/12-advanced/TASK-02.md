# Задание 02. Bean Validation: ограничение длины имени категории
- Тема: Нюансы (Bean Validation)
- Difficulty: 2
- Что нужно сделать: Добавьте в сущность `Category` (`src/main/java/com/example/jpalab/entity/Category.java`) на поле `name` аннотацию `@Size(max = 50)`. Имя категории ровно в 50 символов сохраняется, на 51 — отклоняется `ConstraintViolationException`, строка не появляется.
- Стартовый код:
```java
// TODO: в src/main/java/com/example/jpalab/entity/Category.java
// добавьте на поле name ограничение длины (значение — по условию задания):
// import jakarta.validation.constraints.Size;
private String name; // + аннотация
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t12.TASK02Test#tooLongCategoryNameRejectedByValidation
```
- Критерий приёмки: Тест: категория с именем в 50 символов сохраняется, с именем в 51 символ отклоняется `ConstraintViolationException` (упоминается `name`), в БД после отклонения строки нет.
- Подсказка (не решение): `@Size` проверяет `length()` строки; граница `max` инклюзивна. Как и в задании 01, срабатывает на flush.
