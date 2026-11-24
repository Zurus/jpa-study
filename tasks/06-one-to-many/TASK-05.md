# Задание 05. Ловушка: добавление в коллекцию без обратной ссылки
- Тема: @OneToMany (Category → products)
- Difficulty: 5
- Что нужно сделать: Тест создан заранее (TASK05Test): товар уже сохранён в категории A. Добавьте этот товар ТОЛЬКО в коллекцию категории B — без `product.setCategory(B)`. После flush докажите, что FK не изменился (товар по-прежнему в A), и объясните почему.
- Стартовый код:
```java
@DataJpaTest
class OneToManyTask05Test {
    @Test
    void collectionAloneDoesNotMoveFk() {
        // product в A; B пустая; em.clear();
        // Category b = managed B; Product p = managed товар;
        // b.getProducts().add(p);  // БЕЗ setCategory(b)
        // Statistics.clear(); em.flush();
        // entityUpdateCount == 0; entityInsertCount == 0
        // SELECT category_id WHERE id = p -> id(A)!
    }
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK05Test#collectionAloneDoesNotMoveFk
```
- Критерий приёмки: Тест: после flush `category_id` товара — id категории A (FK НЕ изменился!); статистика: 0 update и 0 insert по товарам (коллекция сама по себе не пишет FK); в комментарии зафиксирован ключевой вывод: владелец FK — ManyToOne-сторона; «много»-сторона может только навигировать, и двунаправленную связь нужно поддерживать с ОБЕИХ сторон.
- Подсказка (не решение): `mappedBy` означает «ключ живёт у тебя (у ManyToOne)» — коллекция не умеет двигать чужой FK.
