# Задание 09. Ловушка: bulk DELETE обходит JPA cascade
- Тема: @Modifying
- Difficulty: 9
- Что нужно сделать: Добавить два bulk DELETE-метода (см. «Стартовый код»): в `ProductRepository` — `deleteProductsInCategory(categoryName)` (удалить все товары категории по имени), в `CategoryRepository` — `deleteCategoriesByName(name)` (удалить категорию по имени). Оба — `@Modifying` + `@Query` (bulk DELETE в JPQL), возвращают число удалённых строк. Тест уже создан (`TASK09Test`): сначала «правильный порядок» удаления (категория B с одним товаром), затем ловушка — bulk DELETE категории A с двумя живыми товарами.
- Стартовый код:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // TODO: добавьте метод с этой сигнатурой:
    // @Modifying + @Query, bulk DELETE товаров по имени категории
    int deleteProductsInCategory(@Param("categoryName") String categoryName);
}

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // TODO: добавьте метод с этой сигнатурой:
    // @Modifying + @Query, bulk DELETE категории по имени
    int deleteCategoriesByName(@Param("name") String name);
}
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t09.TASK09Test#bulkDeleteBypassesCascade
```
- Критерий приёмки: Тест `bulkDeleteBypassesCascade` зелёный: `deleteProductsInCategory("B")` возвращает 1, затем `deleteCategoriesByName("B")` возвращает 1 (сначала «много», потом «один»); bulk DELETE категории A с живыми товарами бросает `DataIntegrityViolationException` (нарушение FK — bulk SQL НЕ выполняет JPA cascade/orphanRemoval); категория A после ловушки осталась в БД (native SELECT); в комментарии записана разница bulk-пути и пути через `em.remove`/repository delete (тема 06: JPA-путь cascade выполняет, bulk SQL — нет).
- Подсказка (не решение): Bulk DELETE не загружает сущности в PC: cascade, orphanRemoval, @Version и lifecycle-колбэки не выполняются вовсе — нарушит FK только правило БД.
