# Задание 03. Bean Validation: валидация на flush, а не на setter
- Тема: Нюансы (Bean Validation)
- Difficulty: 3
- Что нужно сделать: Создайте `@Service` `ProductVersioningService` (имя бина — `productVersioningService`) с методом `void updateProductName(Long id, String name)`: `em.find` → `setName(name)` → `em.flush()` — каждая в СВОЕЙ транзакции (`TransactionTemplate`). Заранее созданный тест (TASK12/TASK03Test) вызывает его с именем `@NotBlank`-нарушением (`"   "`) на существующем `Product.name` и проверяет: (1) сам `setName` с пустым/пробельным значением исключения НЕ бросает; (2) нарушение всплывает только на `em.flush()` как `jakarta.validation.ConstraintViolationException`; (3) UPDATE в БД не выполнялся — имя, цена и `version` в строке не изменились.
- Стартовый код:
```java
// src/main/java/com/example/jpalab/service/ProductVersioningService.java (новый файл)

@Service
public class ProductVersioningService {
    // TODO: em.find(id) -> setName(name) -> em.flush(), своя транзакция
    public void updateProductName(Long id, String name) {
        // TODO
    }
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t12.TASK03Test#validationFiresOnFlushNotOnSetter
```
- Критерий приёмки: Тест: товар сохранён (цена 100.00, version 0); детэчед-`setName("   ")` — тишина; ваш метод бросает `ConstraintViolationException`; в БД имя осталось исходным, цена 100.00, `version = 0`.
- Подсказка (не решение): у Hibernate валидация подключена к событиям flush (pre-insert/pre-update), а не к доступам полей.
