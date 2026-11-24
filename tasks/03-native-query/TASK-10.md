# Задание 10. Native-запрос и LAZY-ассоциации: fetch невозможен
- Тема: Native queries
- Difficulty: 10
- Что нужно сделать: Добавьте в `ProductRepository` native-запрос, возвращающий все товары (`List<Product>`). Тест создан заранее (TASK10Test): 3 товара в разных категориях; после запроса в цикле доступ к `product.getCategory().getName()`; число prepared statements считается через Hibernate Statistics.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с native-запросом (nativeQuery = true, см. «Что нужно сделать»)
    // метод должен иметь точно такое имя (без параметров):
    List<Product> findNativeAll();
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t03.TASK10Test#nativeCannotFetchLazyAssociations
```
- Критерий приёмки: Тест: сам native-SELECT — 1 запрос; `getCategory()` после запроса — ещё прокси (`Hibernate.isInitialized` == false); цикл доступа к категориям добавляет N SELECT (всего 1 + N); в комментарии зафиксированы два пути против N+1 (JPQL `JOIN FETCH` / `@EntityGraph` из тем 2/7) и почему ни один из них не применим «внутри» native-запроса.
- Подсказка (не решение): Hibernate не видит вашу бизнес-логику: какие ассоциации понадобятся — решает время выполнения, запросом по запросу.
