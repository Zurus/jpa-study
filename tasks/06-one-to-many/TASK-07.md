# Задание 07. Каскад удаления: сначала дети, потом родитель
- Тема: @OneToMany (Category → products)
- Difficulty: 7
- Что нужно сделать: Тест создан заранее (TASK07Test): категория с 3 товарами. Удалите только категорию (`categoryRepository.delete` + flush). Докажите, что cascade убрал и товары (4 DELETE: 3 по товарам + 1 по категории), и что порядок операций уважает FK: если бы Hibernate удалил родителя раньше детей, H2 выбросил бы DataIntegrityViolation — сам факт успешного flush доказывает, что `delete from products` ушли ДО `delete from categories`.
- Стартовый код:
```java
@DataJpaTest
class OneToManyTask07Test {
    @Test
    void cascadeRemoveOrder() {
        // category с 3 товарами; saveAndFlush; em.clear();
        // Statistics.clear();
        // Category managed = findById; categoryRepository.delete(managed); em.flush();
        // entityDeleteCount == 4
        // SELECT COUNT(*) products WHERE category_id = ? -> 0
        // SELECT COUNT(*) categories WHERE id = ? -> 0
    }
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK07Test#cascadeRemoveOrder
```
- Критерий приёмки: Тест: после flush и товары, и категория исчезли из БД; статистика: ровно 4 entity-delete (3× по товарам + 1× по категории); в комментарии зафиксировано, что порядок удаления выводится из графа FK-зависимостей (дети раньше родителя), а обратный порядок упёрся бы в FK-ограничение (см. тему 5, задание 08).
- Подсказка (не решение): cascade REMOVE распространяется на коллекцию, а порядок удаления выводится из графа связей.
