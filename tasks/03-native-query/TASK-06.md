# Задание 06. Native + пагинация (Pageable)
- Тема: Native queries
- Difficulty: 6
- Что нужно сделать: Добавьте в `ProductRepository` native-запрос, возвращающий `Page<Product>`: все товары, последний параметр — `Pageable`. Тест создан заранее (TASK06Test): 5 товаров, `pageSize = 2`, обход страниц 0..2.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с native-запросом (nativeQuery = true, см. «Что нужно сделать»)
    // метод должен иметь точно такое имя + Pageable:
    Page<Product> findNativePage(Pageable pageable);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t03.TASK06Test#nativePageable
```
- Критерий приёмки: Тест: страницы 2/2/1, `getTotalElements() == 5`, `getTotalPages() == 3`; в SQL-логе ТЕКСТ запроса не содержит `LIMIT`/`OFFSET` — лимитирование делает JDBC (`setFirstResult`/`setMaxResults`), что зафиксировано комментарием к тесту с цитатой из SQL-лога.
- Подсказка (не решение): `Pageable` работает и с native-запросами — Spring Data подставит смещение и размер через JDBC API.
