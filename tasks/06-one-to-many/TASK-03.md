# Задание 03. orphanRemoval: удаление из коллекции
- Тема: @OneToMany (Category → products)
- Difficulty: 3
- Что нужно сделать: Тест создан заранее (TASK03Test): категория с 2 товарами. Загрузите managed-родителя, найдите в его коллекции нужный managed-товар и удалите его ТОЛЬКО из коллекции (`remove`), не вызывая `em.remove`. После flush докажите, что строка товара исчезла из БД, а второй товар категории на месте.
- Стартовый код:
```java
@DataJpaTest
class OneToManyTask03Test {
    @Test
    void orphanRemoval() {
        // category с 2 товарами; saveAndFlush; em.clear();
        // Category managed = findById;
        // Product removeMe = managed.getProducts().stream()... (managed-элемент!)
        // managed.getProducts().remove(removeMe);  // em.remove НЕ вызывается
        // Statistics.clear(); em.flush(); -> 1 delete
        // SELECT COUNT(*) WHERE id = удалённый -> 0;  WHERE category_id -> 1
    }
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK03Test#orphanRemoval
```
- Критерий приёмки: Тест: после flush удалённый товар исчез из `products` (SELECT по его id пуст), второй товар категории на месте; статистика: ровно 1 prepared statement и 1 delete (без ручного `em.remove`); в комментарии зафиксировано: `orphanRemoval = true` превращает «выпадение из коллекции» в удаление сущности (и неявно влечёт cascade REMOVE).
- Подсказка (не решение): «осиротевший» элемент коллекции при orphanRemoval = удалённая сущность. Удалять из коллекции нужно именно managed-экземпляр.
