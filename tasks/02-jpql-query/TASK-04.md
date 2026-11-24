# Задание 04. Регистронезависимый поиск: UPPER/CONCAT/LIKE
- Тема: JPQL @Query
- Difficulty: 4
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query`, ищущий товары по фрагменту имени без учёта регистра: функция UPPER над именем и над параметром, LIKE с «дичками» `%` по обе стороны от фрагмента (собранного через CONCAT). Тест на именах в смешанном регистре создан заранее (TASK04Test).
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (см. «Что нужно сделать»)
    // метод должен иметь точно такую сигнатуру:
    List<Product> searchByNameFragmentIgnoreCase(@Param("fragment") String fragment);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t02.TASK04Test#upperConcatLikeIsCaseInsensitive
```
- Критерий приёмки: Тест: «WIDG» находит «Super Widget», «super» находит «Super Widget» и «super gizmo»; одиночное «i» из «Boring item» НЕ находит лишнего (фрагмент внутри слова); в SQL-логе UPPER применён и к колонке, и к параметру; сам фрагмент передаётся параметром, а не встраивается в текст запроса.
- Подсказка (не решение): `UPPER(p.name) LIKE UPPER(CONCAT('%', :fragment, '%'))` — дички собираются вокруг параметра.
