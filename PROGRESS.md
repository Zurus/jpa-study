# PROGRESS — jpa-lab

> Рабочее состояние проекта. Последнее обновление — после создания темы 15
> (15-element-collection) по явному запросу.

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

## Состояние (после аудита и создания тем 13–15)

- Текущий охват: 15 тем (01–15), по 10 заданий = 150 заданий и 150
  заранее созданных unit-тестов; пакеты тестов `com.example.jpalab.tasks.t01`…`t15`.
- Тестовые классы: `src/test/java/com/example/jpalab/tasks/<тема>/TASK01Test.java`…`TASK10Test.java`;
  пакеты Java `com.example.jpalab.tasks.t01`…`t15`.
- Стартовый код: сущности `Product` / `Category` (у `Product` —
  `tags: Set<String>` и `attributes: Map<String,String>` через
  `@ElementCollection`, тема 15), `ProductRepository`
  (JpaRepository + JpaSpecificationExecutor + findByCategory +
  ProductSummary), пустой `CategoryRepository`, сервисы-заготовки
  (`ProductService`, `ProductLifecycleService`, `ProductVersioningService`,
  `ProductCriteriaService`, `ProductElementCollectionService`).
- Стартовые методы в репозиториях, раскрывающие решения, удалены
  (в т.ч. «примерные» методы и 14 legacy-тестов, их использующих).
- Все 150 тестов отрабатывают модель «красный до / зелёный после»:
  в базовом состоянии все 150 красные («Задание не реализовано»).
- Все `tasks/*/README.md` и `TASK-XX.md` в одном формате: стартовый код,
  точная команда `mvn -Dtest=<класс>#<метод>`, критерий приёмки,
  подсказка без решения.
- `verify-links.sh` проверяет все 150 ссылок на тесты: ошибок 0.

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
@Where/@SQLRestriction, @SQLDelete (без мягкого удаления — тема 25).

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

## Проверено командой

```bash
cd D:/study/SQLTrain/jpa-lab
bash verify-links.sh     # 150 ссылок, ошибок 0
mvn test                 # Tests run: 150, Failures: 150, Errors: 0, Skipped: 0
```

## План (только по явным запросам)

Темы 16–26, по 10 заданий каждая, те же правила (10 тестов заранее,
красный до / зелёный после, формат TASK-XX.md):

16. Наследование сущностей
17. Продвинутый маппинг типов
18. Полный маппинг коллекций
19. Querydsl
20. Транзакции
21. Пессимистические блокировки
22. Second-level cache
23. Query cache
24. Hibernate Event Listeners
25. Soft delete
26. Производительность
