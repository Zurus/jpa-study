# Задание 06. Stale-коллекция: вставка «со стороны товара»
- Тема: @OneToMany (Category → products)
- Difficulty: 6
- Что нужно сделать: Тест создан заранее (TASK06Test): загрузите категорию A с 1 товаром (коллекция инициализирована, size = 1). НЕ трогая коллекцию, со стороны товара сохраните новый товар с `category = A`. Покажите, что уже загруженная коллекция A «не узнала» о новом товаре (size всё ещё 1, а в БД уже 2), и что `em.refresh` лечит это.
- Стартовый код:
```java
@DataJpaTest
class OneToManyTask06Test {
    @Test
    void staleCollectionAfterSideInsert() {
        // A с 1 товаром; em.clear();
        // Category a = findById; a.getProducts().size(); // 1
        // productRepository.save(new Product с category = a); em.flush();
        // a.getProducts().size(); // 1 (stale!)
        // SELECT COUNT(*) FROM products WHERE category_id = ? -> 2
        // em.refresh(a); a.getProducts().size(); // 2
    }
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK06Test#staleCollectionAfterSideInsert
```
- Критерий приёмки: Тест: после «боковой» вставки уже загруженная коллекция по-прежнему содержит 1 элемент (stale-вид из первого уровня кэша), а в БД товаров у A уже 2 (native COUNT); после `em.refresh(a)` коллекция содержит 2; в комментарии зафиксирован вывод: данные коллекции — это снапшот момента инициализации, внешние изменения не «пробираются» в неё до refresh/новой транзакции.
- Подсказка (не решение): LAZY-коллекция загружается ОДИН раз и кэшируется в persistence context — «из-за спины» она не обновляется.
