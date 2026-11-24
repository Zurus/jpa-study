# Задание 01. Первый GROUP BY
- Тема: GROUP BY / HAVING
- Difficulty: 1
- Что нужно сделать: Добавьте в `ProductRepository` метод `@Query` (JPQL): количество товаров по каждому имени (группировка по `name`), возврат строк `Object[]` [название, количество]. Тест с двумя товарами одного имени и одним с другим (у `Product.name` нет unique-ограничения) создан заранее (TASK01Test).
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте @Query с JPQL (GROUP BY / агрегаты, см. «Что нужно сделать»)
    // метод должен иметь точно такое имя (без параметров):
    List<Object[]> countByName();
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t04.TASK01Test#groupCountsByName
```
- Критерий приёмки: Тест: возвращено 2 строки; первая — ["Alpha", 2], вторая — ["Beta", 1] (количество — `Long`); в SQL-логе `group by name`; в комментарии зафиксировано правило: каждая колонка SELECT без агрегата обязательна в GROUP BY.
- Подсказка (не решение): COUNT(p) считает строки группы; `p.name` — колонка-«ключ», поэтому она и в GROUP BY.
