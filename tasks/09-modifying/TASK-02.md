# Задание 02. Bulk DELETE по условию
- Тема: @Modifying
- Difficulty: 2
- Что нужно сделать: Добавить в `ProductRepository` метод с точной сигнатурой из «Стартового кода»: bulk DELETE в JPQL (`@Modifying` + `@Query`), удаляющий товар(ы) с заданным именем. Метод возвращает число удалённых строк. Тест уже создан (`TASK02Test`): три товара X, Y, Z без категории.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте метод с этой сигнатурой:
    // @Modifying + @Query с bulk DELETE в JPQL (см. «Что нужно сделать»)
    int deleteProductsByName(@Param("name") String name);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t09.TASK02Test#bulkDeleteByCondition
```
- Критерии приёмки: Тест `bulkDeleteByCondition` зелёный: метод возвращает 1; в БД (native SELECT) остались только X и Z (Y удалён); в SQL-логе один вызов `delete from products where name = ?`.
- Подсказка (не решение): Bulk DELETE в JPQL имеет вид `DELETE FROM Сущность e WHERE <условие>`; он убирает строки из БД напрямую — `em.remove`, cascade и lifecycle-колбэки не участвуют.
