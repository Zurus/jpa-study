# Задание 08. «Двойное владение»: managed-товар в коллекции новой категории
- Тема: @OneToMany (Category → products)
- Difficulty: 8
- Что нужно сделать: Тест создан заранее (TASK08Test): товар P сохранён в категории A (managed). Создайте НОВУЮ категорию C, добавьте в её коллекцию этот managed-товар P, НЕ меняя `P.category` (он остаётся A), и сохраните C. Докажите, что после flush FK товара остался A, а коллекция C после `em.refresh` окажется пуста.
- Стартовый код:
```java
@DataJpaTest
class OneToManyTask08Test {
    @Test
    void doubleOwnership() {
        // P в A; em.clear(); managed-товар из A;
        // Category c = new; c.getProducts().add(P); // setCategory НЕ вызываем
        // categoryRepository.save(c); em.flush();
        // SELECT category_id WHERE id = P -> id(A)
        // em.refresh(c); c.getProducts().size() -> 0
    }
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK08Test#doubleOwnership
```
- Критерий приёмки: Тест: после flush `category_id` товара — id A (FK не переехал); сохранение C не сломалось (merge managed-товара — no-op по FK); после `em.refresh(c)` коллекция C пуста (товар ей не принадлежит по FK); в комментарии зафиксирован вывод: «владеть» товаром может только одна категория (FK — единственная истина), а коллекция — лишь её отражение.
- Подсказка (не решение): FK определяет истинного владельца; коллекция «без обратной ссылки» — пустое пожелание.
