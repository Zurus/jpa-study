# Тема 15, задание 09

Тема: Элементные коллекции (@ElementCollection)
Сложность: 9

## Что нужно сделать
Добавить в `ProductElementCollectionService` метод:

```java
public void updateAttributes(Long productId, Map<String, String> updates) {
    // TODO: слить (merge) переданную карту в Product.attributes продукта
}
```

Поведение — слияние, а не замена:
- ключи, которых нет в `updates`, СОХРАНЯЮТСЯ;
- значения существующих ключей обновляются;
- новые ключи добавляются.

«Полная замена» (очистить и вписать `updates`) потеряет сохранённые
ключи — тест будет красным.

## Как проверить задание
```bash
mvn -Dtest=com.example.jpalab.tasks.t15.TASK09Test#updateAttributesMergesInsteadOfReplace
```

## Критерий приёмки
- после вызова в `product_attributes`: сохранённые пары на месте,
  значения общих ключей — новые, новых ключей добавлено ровно столько,
  сколько их было в `updates`;
- строк ровно `|старые ключи ∪ новые ключи|`.

## Подсказка (без решения)
Одна стандартная операция Map делает merge; diff управляемого
Map-а Hibernate применит сам при flush.
