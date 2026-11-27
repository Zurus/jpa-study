# PROGRESS — jpa-lab

> Рабочее состояние проекта. Последнее обновление — после создания темы 17
> (17-inheritance) по явному запросу.

## Текущий статус

- Границы: JavaRush Java 8 → Spring Data JPA (практика темы «JPA»).
- Стек: Java 17, Spring Boot 3.2.x, Spring Data JPA / Hibernate 6, H2,
  Maven, JUnit 5, AssertJ, Lombok, Validation, Flyway.
- Модель обучения: красный до реализации, зелёный после корректной
  реализации. Каждое задание проверяется заранее созданным unit-тестом,
  который красный до студента и зелёный после. Тест не раскрывает решение.
- Студентский артефакт — реальный метод или класс в `src/main/java`;
  тест вызывает его отражённо через `TaskTestSupport`.
- `answers/`: не создаётся и не заполняется.

## Состояние (после аудита и создания тем 13–17)

- Текущий охват: 17 тем (01–17), по 10 заданий = 170 заданий и 170
  заранее созданных unit-тестов; пакеты тестов `com.example.jpalab.tasks.t01`…`t17`.
- Тестовые классы: `src/test/java/com/example/jpalab/tasks/<тема>/TASK01Test.java`…`TASK10Test.java`;
  пакеты Java `com.example.jpalab.tasks.t01`…`t17`.
- Стартовый код: сущности `Product` / `Category` / `Tag` (у `Product` —
  `tags: Set<String>` и `attributes: Map<String,String>` через
  `@ElementCollection`, тема 15, и `productTags: Set<Tag>` — владелец
  `@ManyToMany`, таблица `product_tag_link`, тема 16; у `Tag` —
  `products: Set<Product>` с `mappedBy = "productTags"`),
  иерархия темы 17: `BaseEntity` (`@MappedSuperclass`: `id`, `createdAt`),
  `DigitalProduct` (`downloadUrl`), `PhysicalProduct` (`weight`),
  `ProductRepository`
  (JpaRepository + JpaSpecificationExecutor + findByCategory +
  ProductSummary), пустые `CategoryRepository` и `TagRepository`, сервисы-заготовки
  (`ProductService`, `ProductLifecycleService`, `ProductVersioningService`,
  `ProductCriteriaService`, `ProductElementCollectionService`,
  `ProductTagService`, `ProductHierarchyService`).
- Стартовые методы в репозиториях, раскрывающие решения, удалены
  (в т.ч. «примерные» методы и 14 legacy-тестов, их использующих).
- Все 170 тестов отрабатывают модель «красный до / зелёный после»:
  в базовом состоянии все 170 красные («Задание не реализовано»).
- Все `tasks/*/README.md` и `TASK-XX.md` в одном формате: стартовый код,
  точная команда `mvn -Dtest=<класс>#<метод>`, критерий приёмки,
  подсказка без решения.
- `verify-links.sh` проверяет все 170 ссылок на тесты: ошибок 0.

## Темы (текущий охват)

### 01. Репозиторные запросы (10)
`ProductRepository`: 10 derived-методов с `@Transactional(readOnly = true)`.

### 02. JPQL (10)
`ProductRepository`: 10 `@Query` JPQL.

### 03. Нативные запросы (10)
`ProductRepository`: 10 `@Query(nativeQuery = true)`.

### 04. Агрегация (10)
`ProductRepository`: 10 `@Query` с `GROUP BY`/`HAVING`.

### 05. @ManyToOne (10)
`ProductLifecycleService`: 10 методов (категории, каскады, flush/clear,
наличие категорий).

### 06. @OneToMany (10)
`ProductLifecycleService`: 10 методов ( mappedBy, orphanRemoval,
обновление связей, статистика категорий).

### 07. N+1 (10)
`ProductLifecycleService`: 10 методов (fetch join, @EntityGraph,
сравнение SQL-вызовов).

### 08. Пагинация (10)
`ProductRepository` + `ProductLifecycleService`: Page/Pageable, slice,
count, границы страниц, Pageable.from.

### 09. Модифицирующие запросы (10)
`ProductRepository`: 10 `@Modifying` JPQL.

### 10. Проекты (10)
`ProductRepository`: 10 `@Query` с проекциями
(конструктор/интерфейс/new, ORDER BY на проекции).

### 11. Жизненный цикл (10)
`ProductVersioningService` + `ProductRepository`: persist/merge,
transient/detached, @Version.

### 12. Продвинутые темы (10)
`ProductRepository` + `ProductLifecycleService`: @Lob, @Transient,
расширения/конвертеры, @DynamicUpdate/@DynamicInsert,
@Where/@SQLRestriction, @SQLDelete (без мягкого удаления — тема 26).

### 13. Specification (10)
Студентский артефакт: `com.example.jpalab.repository.ProductSpecifications`
(публичный класс без аргументов, 10 статических методов,
возвращают `Specification<Product>`): точное имя; цена;
null-фрагмент; AND (join по категории); OR; NOT (isNotNull + not like);
3 опциональных фильтра (null-guard); join по категории + Pageable;
precedence `name AND (price OR stock)`; открытый диапазон + категория
(null-категория при пустом имени).

### 14. Criteria API (10)
Студентский артефакт: методы
`com.example.jpalab.service.ProductCriteriaService` (стартовый код —
`@Service` + `@PersistenceContext EntityManager`, методов нет):
`findByName(String)`; `findPriceBetween(BigDecimal, BigDecimal)`;
`findNamesByStockAtLeast(int)` (скалярный `List<String>`);
`findByCategoryName(String)` (inner join);
`findByCategoryNameLeftJoin(String)` (left join, null-фильтр);
`findSortedPriceDescNameAsc(BigDecimal)` (price DESC + name ASC);
`findDynamic(List<String>, BigDecimal, boolean)` (динамический where);
`sumPricesInCategory(String)` (`CriteriaQuery<BigDecimal>`);
`findInCategoriesWithPriceAtLeast(BigDecimal)` (subquery, без null-категорий);
`findCategoryNamesWithAveragePriceAtLeast(BigDecimal)`
(group by + having, скалярный `List<String>`).

### 15. Элементные коллекции @ElementCollection (10)
Студентский артефакт: методы
`com.example.jpalab.service.ProductElementCollectionService` (стартовый код —
`@Service` + `@PersistenceContext EntityManager`, методов нет);
стартовый код сущности: у `Product` добавлены
`tags: Set<String>` (`@ElementCollection`, таблица `product_tags`) и
`attributes: Map<String, String>` (`@ElementCollection` + `@MapKeyColumn`,
таблица `product_attributes`, обе с `@Builder.Default`):
`addTag(Long, String)`; `removeTag(Long, String)`;
`setAttribute(Long, String, String)`; `removeAttribute(Long, String)`;
`findProductIdsByTag(String)` (join по Set); `findProductIdsByAttribute(String, String)`
(KEY(a)/VALUE(a)); `findProductIdsWithAnyTag(List)` (union + защита пустого списка);
`syncTags(Long, Set)` (полная замена); `updateAttributes(Long, Map)` (merge, не replace);
`persistProductWithPreparedCollection(Product)` (дефолтный каскад PERSIST
элементных коллекций). Ловушки проверены эмпирически: add-only sync → T08
красный; full-replace update → T09 красный.
Важный факт Hibernate 6: путь `a.key`/`a.value` по элементной Map не
поддерживается (UnknownPathException); работают функции `KEY(a)`/`VALUE(a)`.

### 16. @ManyToMany (10)
Студентский артефакт: методы
`com.example.jpalab.service.ProductTagService` (стартовый код —
`@Service` + `@PersistenceContext EntityManager`, методов нет);
стартовый код сущностей: `Tag` (`id`, `name`, `products: Set<Product>` —
`mappedBy = "productTags"`) и поле-владелец `Product.productTags: Set<Tag>`
(`@ManyToMany` + `@JoinTable(name = "product_tag_link")`); пустой
`TagRepository`. Имя поля — `productTags`, потому что `tags` в `Product`
уже занято строковой коллекцией темы 15:
`addTagToProduct(Long, Tag)`; `removeTagFromProduct(Long, Tag)`;
`linkBothSides(Long, Tag)` (двунаправленная согласованность в PC);
`unlinkBothSides(Long, Tag)`; `deleteTagKeepsProducts(Long)` (cascade = NONE,
сначала обрыв связей со стороны владельца; `tag.getProducts()` может быть
stale — надёжнее JPQL-join); `findProductIdsByTagName(String)`;
`findAllWithTags()` (LEFT JOIN FETCH, ровно 1 SQL — N+1 на `@ManyToMany`);
`findProductsSharingTagWith(Long)` (distinct, сам продукт исключён);
`findSharedTagNames(Long, Long)` (пересечение, симметрично);
`syncProductTags(Long, Set)` (полная замена связей). Ловушки проверены
эмпирически: одно-стороннее link/unlink → T03/T04 красные; наивный
`em.remove(tag)` → T05 красный (FK на `product_tag_link`); fetch без join
→ T07 красный (1+N запросов); включение самого продукта → T08 красный;
add-only sync → T10 красный.

### 17. Наследование сущностей (10)
Студентский артефакт: методы
`com.example.jpalab.service.ProductHierarchyService` (стартовый код —
`@Service` + `@PersistenceContext EntityManager`, методов нет);
стартовый код сущностей: `BaseEntity` (`@MappedSuperclass`: `id`,
`createdAt`), `DigitalProduct` (`downloadUrl`), `PhysicalProduct`
(`weight`); иерархии `com.example.jpalab.entity.vehicle.*` (JOINED)
и `com.example.jpalab.entity.document.*` (TABLE_PER_CLASS) создаёт
студент (тесты находят их через `TaskTestSupport.newInstance` + рефлексию):
`saveDigitalProduct(String, LocalDateTime)`;
`savePhysicalProduct(Double, LocalDateTime)`; перевод основной
иерархии на SINGLE_TABLE (аннотации: `@Entity`-корень, `@Inheritance`,
`@DiscriminatorColumn(name = "ptype")`, таблица `products_base`);
`@DiscriminatorValue("DIGITAL")`/`"PHYSICAL"`;
`findAllProducts()` (полиморфный запрос по корню, порядок по id);
`findPhysicalProductIdsByWeightAtLeast(Double)`; иерархия
Vehicle/Car/Bus (persist = 2 prepared statement, дискриминатор `vtype`);
иерархия Document/PdfDocument/OdtDocument (SEQUENCE вместо IDENTITY,
полиморфный запрос = 1 UNION);
`findDigitalProductIdsWithoutDownloadUrl()` (NULL-ловушка
полиморфной таблицы); `persistStatementCounts()`
(singleTable=1, joined=2, tablePerClass=2). Ловушки проверены
эмпирически: JOINED вместо SINGLE_TABLE → T03 красный; без
`@DiscriminatorValue` → T04 красный (в колонке имя класса, а не
значение); SINGLE_TABLE у Vehicle → T07 красный (1 prepared statement);
нативный SQL без дискриминатора → T09 красный. Важные факты
Hibernate 6: `@Inheritance` на `@MappedSuperclass` игнорируется
(корень обязан быть `@Entity`); TABLE_PER_CLASS несовместим с
IDENTITY (требуется SEQUENCE).

## Проверено командой

```bash
cd D:/study/SQLTrain/jpa-lab
bash verify-links.sh     # 170 ссылок, ошибок 0
mvn test                 # Tests run: 170, Failures: 168, Errors: 2, Skipped: 0
```

## План (только по явным запросам)

Темы 18–27, по 10 заданий каждая, те же правила (10 тестов заранее,
красный до / зелёный после, формат TASK-XX.md):

18. Продвинутый маппинг типов
19. Полный маппинг коллекций
20. Querydsl
21. Транзакции
22. Пессимистические блокировки
23. Second-level cache
24. Query cache
25. Hibernate Event Listeners
26. Soft delete
27. Производительность
