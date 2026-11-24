# Задание 01. Первый OneToMany: каскад сохранения коллекции
- Тема: @OneToMany (Category → products)
- Difficulty: 1
- Что нужно сделать: Тест создан заранее (TASK01Test): создайте категорию, в её коллекцию `products` добавьте 2 товара (не забудьте обратную ссылку `product.setCategory(category)`) и сохраните только КАТЕГОРИЮ. Докажите, что cascade сохранил и товары.
- Стартовый код:
```java
@DataJpaTest
class OneToManyTask01Test {
    @Test
    void cascadePersistsCollection() {
        // category.builder()...build(); category.getProducts().add(p1); add(p2)
        // (p1/p2 связаны обратно: setCategory)
        // categoryRepository.save(category); em.flush();
        // Statistics: 3 insert (1 categories + 2 products)
        // SELECT COUNT(*) FROM products WHERE category_id = ? -> 2
    }
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK01Test#cascadePersistsCollection
```
- Критерий приёмки: Тест: сохранена только категория (`save` один раз), но в БД 2 строки товаров с `category_id`, равным id категории; статистика: 3 insert (1 `categories` + 2 `products`); в комментарии зафиксировано, что `cascade = ALL` включает PERSIST/MERGE.
- Подсказка (не решение): каскад идёт ОТ родителя к коллекции: сохранение «одного» уносит «многих».
