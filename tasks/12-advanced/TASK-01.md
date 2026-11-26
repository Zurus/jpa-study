# Задание 01. Bean Validation: ограничение длины имени товара
- Тема: Нюансы (Bean Validation)
- Difficulty: 1
- Что нужно сделать: Добавьте в сущность `Product` (`src/main/java/com/example/jpalab/entity/Product.java`) на поле `name` аннотацию `@Size(max = 100)`. Граница инклюзивная: имя ровно в 100 символов сохраняется, имя на 101 символ отклоняется `jakarta.validation.ConstraintViolationException` на flush/commit, транзакция откатывается, строка в БД не появляется.
- Стартовый код:
```java
// TODO: в src/main/java/com/example/jpalab/entity/Product.java
// добавьте на поле name ограничение длины (значение — по условию задания):
// import jakarta.validation.constraints.Size;
private String name; // + аннотация
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t12.TASK01Test#tooLongNameRejectedByValidation
```
- Критерий приёмки: Тест: имя на 100 символов сохраняется (строка в БД), имя на 101 символ бросает `ConstraintViolationException` с упоминанием `name`, после отклонения строки в БД нет.
- Подсказка (не решение): ограничения Bean Validation в JPA проверяются на `flush` (до SQL), а не на `persist`; исключение — `jakarta.validation.ConstraintViolationException`.
