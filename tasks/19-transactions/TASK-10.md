# TASK-10. Пакет операций: каждая — собственная транзакция

Тема: `@Transactional`: propagation и rollback. Сложность: 10/10.

## Что делать

Добавить два метода в `com.example.jpalab.service.ProductService`:

```java
@Transactional
public int processStockTransfers(Long fromId, Long toId,
                                 List<Integer> amounts, int maxToStock) {
    // TODO
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void transferStockAtomically(Long fromId, Long toId,
                                    int amount, int maxToStock) {
    // TODO
}
```

Поведение:

- `processStockTransfers` во внешней транзакции прогоняет каждый
  `amount` из списка через `transferStockAtomically`, ловит
  `IllegalStateException` от каждого срыва и продолжает; возвращает
  число завершившихся переносов;
- вызов `transferStockAtomically` обязательно должен пройти через
  прокси Spring (self-инъекция: `@Autowired @Lazy` поле самого
  сервиса) — иначе уровень propagation не применится;
- `transferStockAtomically`: снять `amount` у источника, прибавить
  получателю, `flush`; если склад получателя превысил `maxToStock` —
  `IllegalStateException`; каждая такая операция живёт в собственной
  транзакции — сбой откатывает только её.

## Как проверить

```bash
mvn -Dtest=com.example.jpalab.tasks.t19.TASK10Test#batchTransfersCommitIndependently
```

## Критерии

- склады 10 и 5, `amounts = [2, 2, 1]`, `maxToStock = 8`;
- первая операция (5→7) и третья (7→8) завершены; вторая (7→9 > 8)
  отклонена и откатилась только сама;
- метод возвращает 2;
- в БД: 7 и 8 — применены только успешные переносы.

## Подсказка

Два нюанса: (1) объявляющая транзакция на внешнем методе здесь —
«рама» для цикла и catch, а не общая судьба — каждая операция
закоммичивается отдельно; (2) self-вызов внутри одного бина обходит
прокси, поэтому внутренний метод вызывают через проксированное поле, а
не через `this`.
