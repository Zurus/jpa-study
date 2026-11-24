# Задание 09. Каскад 25 товаров: N INSERT-ов без батчинга
- Тема: @OneToMany (Category → products)
- Difficulty: 9
- Что нужно сделать: Тест создан заранее (TASK09Test): сохраните категорию с 25 товарами в коллекции (один `save`). Посчитайте через Hibernate Statistics число выполненных `INSERT` и зафиксируйте, что по умолчанию каждый товар — отдельный prepared statement (без JDBC-батчинга). Обоснуйте в комментарии, как это оптимизируется.
- Стартовый код:
```java
@DataJpaTest
class OneToManyTask09Test {
    @PersistenceContext private EntityManager em;

    @Test
    void cascadeInsertsCount() {
        // категория с 25 товарами;
        // Statistics.clear(); save(category); em.flush();
        // entityInsertCount == 26 (1 categories + 25 products)
        // prepareStatementCount == 26 -> каждый INSERT отдельный
    }
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK09Test#cascadeInsertsCount
```
- Критерий приёмки: Тест: статистика показывает 26 entity-insert (1 `categories` + 25 `products`) и 26 prepared statement — без батчинга каждый каскадный INSERT отдельный; в комментарии объяснено: включение `hibernate.jdbc.batch_size` (>1) и сортировки DML (`hibernate.order_inserts`) превращает 25 отдельных INSERT-ов в батч; сам тест батчинг НЕ включает (только измеряет).
- Подсказка (не решение): cascade PERSIST по большой коллекции = N отдельных INSERT-ов, если JDBC не настроен на батчи.
