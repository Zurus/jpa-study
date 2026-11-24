# Задание 02. Добавление в коллекцию managed-родителя
- Тема: @OneToMany (Category → products)
- Difficulty: 2
- Что нужно сделать: Тест создан заранее (TASK02Test): сохраните пустую категорию и загрузите её заново (managed). Добавьте в её коллекцию новый товар с обратной ссылкой и выполните flush. Докажите, что товар появился в БД с корректным FK.
- Стартовый код:
```java
@DataJpaTest
class OneToManyTask02Test {
    @Test
    void addToManagedCollection() {
        // save(пустая категория); em.clear();
        // Category managed = findById;
        // newProduct.setCategory(managed); managed.getProducts().add(newProduct);
        // Statistics.clear(); em.flush(); -> ровно 1 insert
        // SELECT COUNT(*) FROM products WHERE category_id = ? -> 1
    }
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK02Test#addToManagedCollection
```
- Критерий приёмки: Тест: после flush в `products` появилась строка с `category_id` = id категории; статистика: ровно 1 insert и 0 update (категория не пересоздавалась); `managed.getProducts().size() == 1`; в комментарии зафиксировано: изменение коллекции managed-родителя отслеживается, новый элемент уходит в БД через cascade PERSIST.
- Подсказка (не решение): коллекция managed-сущности «под контролем» — Hibernate видит добавленный элемент при flush.
