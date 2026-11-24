# Задание 10. Откат транзакции: коллекция и строки отменяются
- Тема: @OneToMany (Category → products)
- Difficulty: 10
- Что нужно сделать: Тест создан заранее (TASK10Test) в РЕАЛЬНЫХ транзакциях (`@SpringBootTest` + `TransactionTemplate`): в одной транзакции добавьте 2 товара в коллекцию managed-категории (flush внутри транзакции!), а затем завершите транзакцию ОТКАТОМ (исключение). Докажите в новой транзакции: товаров в БД нет, свежая загрузка коллекции категории пуста, и «внутренний» flush ничего не зафиксировал.
- Стартовый код:
```java
@SpringBootTest
class OneToManyTask10Test {
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TransactionTemplate tx;
    @PersistenceContext private EntityManager em;

    @Test
    void rollbackUndoesCollectionInserts() {
        // setup tx: пустая категория (commit)
        // assertThatThrownBy(() -> inTx(() -> {
        //     managed.getProducts().add(p1); add(p2);
        //     em.flush();            // INSERT-ы внутри транзакции
        //     throw new RuntimeException("rollback");
        // }));
        // новая tx: COUNT products по категории -> 0; fresh коллекция пуста
    }
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t06.TASK10Test#rollbackUndoesCollectionInserts
```
- Критерий приёмки: Тест: после rollback `SELECT COUNT(*) FROM products WHERE category_id = ?` — 0 (внутренний `flush` выполнил INSERT-ы в рамках транзакции, но rollback их отменил); свежая коллекция категории пуста; в комментарии зафиксировано: flush ≠ commit — flush лишь «выталкивает» SQL в транзакцию, а rollback отменяет всё.
- Подсказка (не решение): `em.flush()` — внутри транзакции; откатив транзакцию, вы откатываете и все её flushed-операции.
