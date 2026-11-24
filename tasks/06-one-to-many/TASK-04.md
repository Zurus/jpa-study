# Задание 04. Перенос товара между категориями
- Тема: @OneToMany (Category → products)
- Difficulty: 4
- Что нужно сделать: Тест создан заранее (TASK04Test): товар в категории A. Перенесите его в категорию B тремя операциями: удалите из коллекции A, добавьте в коллекцию B и обновите обратную ссылку `setCategory(B)`. Докажите, что БД получила ОДИН UPDATE внешнего ключа, а не DELETE + INSERT.
- Стартовый код:
```java
@DataJpaTest
class OneToManyTask04Test {
    @Test
    void moveBetweenCategories() {
        // A с товаром, B пустая; em.clear();
        // a = findById(A); b = findById(B); p = a.getProducts().get(0);
        // a.getProducts().remove(p); b.getProducts().add(p); p.setCategory(b);
        // Statistics.clear(); em.flush();
        // entityUpdateCount == 1; insert/delete == 0
        // SELECT category_id WHERE id = p -> id(B)
    }
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK04Test#moveBetweenCategories
```
- Критерий приёмки: Тест: после flush `category_id` товара — id категории B; товар всё тот же (id не изменился, в `products` ровно 1 строка); статистика: ровно 1 UPDATE и 0 INSERT/DELETE по товару; в комментарии зафиксировано: «перенос» — это смена FK, а не пересоздание строки.
- Подсказка (не решение): коллекция — это навигация; реальное изменение — FK-колонка на стороне товара.
