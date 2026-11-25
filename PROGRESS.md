# PROGRESS.md — JPA Lab

- Дата: 2026-07-11 (повторный аудит по чек-листу + очистка решений из tasks/).
- Команда: `mvn test` (из корня `jpa-lab`).

## Аудит (состояние на 2026-07-11)

- `tasks/`: темы `01`–`08`, **80 TASK-файлов** (10 в каждой). Тем `09`–`12` в `tasks/` нет.
- Тесты: **80 тестовых классов** `TASK01Test`…`TASK10Test` в пакетах `com.example.jpalab.tasks.t01`…`t08` + 34 example-теста.
- `answers/` отсутствует и не создаётся. Решений в `src/` нет.
- `TaskTestSupport.invoke(...)`: рефлексивный вызов целевого метода; до реализации — `AssertionError("Задание не реализовано: ...")`.

## Повторная проверка соответствия (чек-лист, 2026-07-11)

| # | Пункт | Результат |
|---|---|---|
| 1 | В каждой теме ровно 10 `TASK-01.md`…`TASK-10.md` | **OK**: 8/8 тем по 10 файлов |
| 2 | На каждое задание один тестовый класс | **OK**: 80 классов, имя `TASK<NN>Test` соответствует номеру |
| 3 | В задании указан корректный тест (`mvn -Dtest=FQCN#method`) | **OK**: 80/80 — FQCN существует, пакет совпадает, метод в классе есть |
| 4 | Тест проверяет именно требование задания | **OK**: целевые методы совпадают с «Что нужно сделать»; 39 зелёных тестов проверены опытом; 41 красный — через `invoke` по фиксированному имени |
| 5 | `difficulty` 1–10 | **OK**: 80/80 |
| 6 | Нет дублей между темами | **OK**: единственное пересечение имён — `findByPriceGreaterThan` (тема 01: `List<Product>, BigDecimal`; тема 08: `Page<Product>, BigDecimal + Pageable`) — разные перегрузки, разные задачи |
| 7 | Нет решений внутри `tasks/` | **БЫЛО НАРУШЕНИЕ → исправлено**: в 29 файлах (02×8, 03×8, 04×9, 07×3, 08×1) блок «Стартовый код» содержал полное `@Query`/`@EntityGraph` с текстом запроса — готовое решение. Заменено на `// TODO: ...` + требуемая сигнатура метода (контракт с тестом сохранён). Демонстрационные задачи (02/08, 02/10, 03/07, 03/09, 04/09, 07/09) содержат закомментированные иллюстративные примеры — это материал задачи «наблюдайте и объясните», не решение |

## Итог `mvn test` (после правки темы 02)

```
Tests run: 114, Failures: 42, Errors: 0, Skipped: 0
```

- 34 example-теста — все зелёные.
- 38 task-тестов — зелёные (задачи, не требующие кода студента).
- 42 task-теста — красные **как задумано**: `AssertionError("Задание не реализовано: ...")` (0 неожиданных сбоев).

## Тема 01 (Derived queries) — проверка 10/10 (2026-07-11)

- Все 10 тестовых классов существуют: `src/test/java/com/example/jpalab/tasks/01-derived-queries/TASK01Test`…`TASK10Test`.
- **Недостающих тестов нет — ничего не создавалось.**
- Каждое задание ↔ тест сверено: `@DataJpaTest` + AssertJ, минимальные данные через `save`, целевой метод — через `TaskTestSupport.invoke` (без `@Query`, только derived queries, как требует тема):
  - 01 `findByStockQuantityGreaterThan` · 02 `findByNameIgnoreCase` · 03 `findTop5ByOrderByPriceDesc` · 04 `findByNameContainingIgnoreCaseAndPriceBetween` · 05 `findFirstByCategoryName` · 06 `findByNameContainingOrPriceLessThanEqual` · 07 `findByNameLikeIgnoreCase(Pageable)` · 08 `findByCategoryIsNull` + `findByCategoryIsNotNull` · 09 `findByPriceGreaterThan` · 10 `findByPriceGreaterThanAndPriceLessThanEqual`.
- `mvn test`: все 10 красные **как задумано** — `AssertionError("Задание не реализовано: ...")`; неожиданных сбоев нет. Убран старый отчёт `ScratchDbStateTest` из `target/surefire-reports` (класс давно удалён, в сборку не попадает).

## Тема 02 (@Query JPQL) — проверка 10/10 (2026-07-11)

- Все 10 тестовых классов существуют: `src/test/java/com/example/jpalab/tasks/02-jpql-query/TASK01Test`…`TASK10Test`; недостающих не было.
- **Исправлено нарушение спецификации:** старое задание 06 было «Constructor expression (DTO-проекция)» по существующему методу `namePricesAtLeast` — а в спеце тема 02: «Не включать constructor expression — это тема 10». Задание 06 и его тест заменены на соответствующую теме задачу «Выборка одного поля»:
  - новый метод-цель: `List<String> findNamesByPriceBetween(@Param("from") BigDecimal from, @Param("to") BigDecimal to)` (в репозитории отсутствует — тест красный до реализации);
  - `TASK06Test` переписан: `@DataJpaTest`, 4 товара, `TaskTestSupport.invoke`, проверка границ диапазона (оба конца исключены) и ORDER BY по имени; AssertJ.
  - `tasks/02-jpql-query/README.md`: строка «Чему учит» и строка таблицы обновлены (constructor expression убран из темы).
- Остальные 9 задач сверены: `@DataJpaTest` + AssertJ, минимальные данные, целевой метод через `invoke` (01 `findPricedAbove`, 02 `findPricedAboveSortedDeterministic`, 03 `findProductsOfCategory`, 04 `searchByNameFragmentIgnoreCase`, 05 `idNamePriceAbove`, 07 `findPageByPriceBetween`, 09 `findProductsInCategoriesLike`) либо динамический JPQL через `EntityManager` в показательных задачах (08 — колонка vs свойство, 10 — L1-кэш; обе зелёные, код теста — материал наблюдения).
- Итог по теме: 8 красных (ожид.) + 2 зелёных (08, 10).

## Тема 03 (@Query native SQL) — проверка 10/10 (2026-07-11)

- Все 10 тестовых классов существуют: `src/test/java/com/example/jpalab/tasks/03-native-query/TASK01Test`…`TASK10Test`; **недостающих нет — ничего не создавалось**.
- Каждое задание ↔ тест сверено: `@DataJpaTest` + AssertJ, минимальные данные, целевой метод через `TaskTestSupport.invoke` либо динамический SQL/EntityManager в показательных задачах:
  - 01 `findNativePricedAbove` · 02 `findNativePricedAtLeastWithColumns` · 03 `nativeSearchByPriceAbove` (существующий метод, зелёный) · 04 `findNativeByCategoryName` · 05 `nativePricesByName` · 06 `findNativePage` · 08 `findNativeWithoutCategory` · 10 `findNativeAll`;
  - 07 «Ловушка: constructor expression в native не работает» и 09 «native-результат попадает в persistence context» — показательные (зелёные); задание 07 соответствует спеце: демонстрирует **ограничение** native SQL («difference from JPQL … ограничения»), а не реализацию constructor expression.
- Нарушений спецификации темы не найдено. Итог по теме: 7 красных (ожид.) + 3 зелёных (03, 07, 09).

## Тема 04 (GROUP BY / HAVING) — проверка 10/10 (2026-07-11)

- Все 10 тестовых классов существуют: `src/test/java/com/example/jpalab/tasks/04-group-by/TASK01Test`…`TASK10Test`; недостающих не было.
- **Исправлено нарушение спецификации:** старое задание 07 было «Constructor expression по группам» (`categoryNameWithSumOfPrices`, `SELECT new ...`) — constructor expression по спеце относится теме 10, а в скоупе темы 04 (GROUP BY, HAVING, COUNT, SUM, AVG, MIN, MAX, группировка по связанной сущности) его нет; при этом **AVG** из списка темы не покрывался ни одним заданием. Задание 07 и тест заменены на «AVG + HAVING: категории по средней цене»:
  - новый метод-цель: `List<Object[]> categoryAvgPriceAbove(@Param("minAvg") BigDecimal minAvg)` (LEFT JOIN от категории, GROUP BY, строгий HAVING по AVG, ORDER BY по имени; в репозитории отсутствует — тест красный до реализации);
  - `TASK07Test` переписан: `@DataJpaTest`, 2 категории с товарами (AVG 60 и 200) + 1 пустая (AVG NULL), `TaskTestSupport.invoke`, AssertJ; проверяются граница (60 исключена) и то, что пустая группа никогда не возвращается;
  - `tasks/04-group-by/README.md`: строка «Чему учит» (добавлен AVG) и строка таблицы обновлены.
- Остальные 9 задач сверены: `@DataJpaTest` + AssertJ, минимальные данные, целевой метод через `invoke` (01 `countByName`, 02 `countByNameByGroupSizeDesc`, 03 `countByCategoryName`, 04 `categoryAggregates`, 05 `categoryCountsAtLeast`, 06 `productCountPerCategory`, 08 `nullAggregateRows`, 10 `categoryCountPage`) либо динамический JPQL через `EntityManager` в показательной задаче 09 (алиас агрегата в HAVING; зелёный).
- Итог по теме: 9 красных (ожид.) + 1 зелёный (09).

## Тема 05 (@ManyToOne + @JoinColumn) — проверка 10/10 (2026-07-11)

- Все 10 тестовых классов существуют: `src/test/java/com/example/jpalab/tasks/05-many-to-one/TASK01Test`…`TASK10Test`; **недостающих нет — ничего не создавалось**.
- Задания темы — наблюдение/проверка СУЩЕСТВУЮЩЕГО маппинга (`@ManyToOne @JoinColumn @Lazy` уже в `Product`): тесты зелёные без кода студента, его «реализация» — понимание поведения + фиксация выводов в комментариях (по критериям приёмки). Все 10: минимальные данные, AssertJ, проверка реального состояния БД через native-SELECT/исключения;
  - 01 FK NULL→id (insert/update FK) · 02 смена FK одним UPDATE · 03 отвязка (`category_id = NULL`) · 04 transient-категория без cascade (`@SpringBootTest` + `TransactionTemplate`, зафиксированы `InvalidDataAccessApiUsageException`/причина `TransientPropertyValueException`) · 05 identity + прокси · 06 один UPDATE с FK и полем · 07 FK гарантирует БД (native INSERT с чужим id → `ConstraintViolationException`) · 08 удаление «одного» при живых «многих» · 09 merge отстёгнутого (version+1, isNotSameAs) · 10 rollback возвращает FK (`@SpringBootTest` + `TransactionTemplate`);
  - **LAZY/EAGER с SQL-поведением (требование промпта) — покрыто:** задание 05 проверяет прокси (`Hibernate.isInitialized == false` до первого доступа) и количество запросов через Hibernate Statistics (ровно 1 SELECT на категорию для двух товаров); задание 06 — ровно 1 UPDATE (`getEntityUpdateCount == 1`);
- Нарушений спецификации темы не найдено. Итог по теме: 10 зелёных (наблюдательные, ожидаемо), 0 красных.

## Тема 06 (@OneToMany + mappedBy) — проверка 10/10 (2026-07-11)

- Все 10 тестовых классов существуют: `src/test/java/com/example/jpalab/tasks/06-one-to-many/TASK01Test`…`TASK10Test`; **недостающих нет — ничего не создавалось**.
- Задания темы — наблюдение/проверка СУЩЕСТВУЮЩЕГО маппинга (`@OneToMany(mappedBy = "category")` + `cascade = ALL` + `orphanRemoval = true` уже в `Category`): все 10 зелёные без кода студента (наблюдательные, ожидаемо); каждый тест: минимальные данные, AssertJ, и **проверка фактического изменения данных в БД** (native-SELECT `COUNT(*)`/значений после `em.clear()`/`flush`, либо исключения) — в том числе по требованию промпта для cascade и orphanRemoval;
  - 01 cascade PERSIST/MERGE коллекции (2 товара → 2 строки в БД) · 02 добавление в managed-коллекцию (insert, FK = родителя) · 03 **orphanRemoval**: удаление только из коллекции → ровно 1 DELETE (`getEntityDeleteCount == 1`), строка исчезла (native COUNT = 0), вторая на месте · 04 перенос товара A→B (UPDATE FK, 1 строка) · 05 ловушка: коллекция без обратной ссылки FK не двигает (владелец — `Product.category`) · 06 stale-коллекция при вставке «со стороны товара» · 07 cascade REMOVE: дети удаляются раньше родителя (порядок SQL) · 08 «двойное владение» managed-товара · 09 25 товаров → 25 INSERT без батчинга (Hibernate Statistics) · 10 rollback: вставки в коллекцию отменяются (`@SpringBootTest` + `TransactionTemplate`);
- Нарушений спецификации темы не найдено. Итог по теме: 10 зелёных (наблюдательные, ожидаемо), 0 красных.

## Тема 07 (N+1) — проверка 10/10 (2026-07-11)

- Все 10 тестовых классов существуют: `src/test/java/com/example/jpalab/tasks/07-n-plus-one/TASK01Test`…`TASK10Test`; **недостающих нет — ничего не создавалось**.
- **Требование промпта** («проверяй количество SQL-запросов, JOIN FETCH или @EntityGraph») — выполнено: количество запросов измеряется через Hibernate Statistics (`getPrepareStatementCount`) в 01, 02, 03, 04, 05, 06, 07, 08, 10;
  - **4 красных (методы добавляет студент, вызов через `TaskTestSupport.invoke`):** 03 `findAllWithCategoryJoined()` (JOIN FETCH to-one, ровно 1 SELECT, категории инициализированы) · 04 `findAllByPriceGreaterThan(BigDecimal)` (derived + `@EntityGraph(attributePaths = "category")`, 1 SELECT) · 05 `findWithCategoryByNameFetched(String)` (JOIN FETCH с WHERE по имени категории и ORDER BY) · 07 «сравнение LAZY vs JOIN FETCH vs @EntityGraph» — 3 фазы: 1+N (5 запросов на 4 товара) → 1 (JOIN FETCH, задание 03) → 1 (@EntityGraph, задание 04);
  - **6 зелёных (наблюдательные, ожидаемо):** 01 N+1 на @ManyToOne (1+N) · 02 N+1 на @OneToMany (1+N) · 06 batch fetching `default_batch_fetch_size` (прокси схлопываются в один запрос) · 08 N+1 под пагинацией (страница не лечит N+1) · 09 ловушка: count с JOIN FETCH и «мультипликация» строк (динамический JPQL) · 10 сервисный сценарий в реальной транзакции (наивно vs fetch, `@SpringBootTest` + `TransactionTemplate`);
- Нарушений спецификации темы не найдено. Итог по теме: 6 зелёных + 4 красных (ожид.).

## Тема 08 (Пагинация) — проверка 10/10 (2026-07-11)

- Все 10 тестовых классов существуют: `src/test/java/com/example/jpalab/tasks/08-pagination/TASK01Test`…`TASK10Test`; **недостающих нет — ничего не создавалось**.
- **Требование промпта** («содержимое, номер страницы, размер, сортировка, следующая страница») — выполнено: содержимое (`getContent`) проверяют все 10; полный набор метаданных (`getNumber`, `getSize`, `getTotalElements`, `getTotalPages`, `hasNext`, `isLast`) — задание 03; сортировка — задание 02 (asc/desc из вызывающего кода + продолжение ГЛОБАЛЬНОГО порядка на 2-й странице); `hasNext` — в 03–07 и 10;
  - **4 красных (методы добавляет студент, вызов через `TaskTestSupport.invoke`):** 04 `findByPriceGreaterThan(BigDecimal, Pageable)` (Page + фильтрация, count идёт по предикату) · 05 `findByNameContainingIgnoreCase(String, Pageable)` (фильтр + сортировка + пагинация) · 09 `findAllWithCategoryGraph(Pageable)` (@EntityGraph лечит N+1 на странице, 1 запрос вместо 1+N) · 10 `sliceByNameContainingIgnoreCase(String, Pageable)` (Slice + фильтр + Sort: «лишняя» строка ведёт `hasNext`);
  - **6 зелёных (наблюдательные, ожидаемо):** 01 первый Page (2 SQL: count + данные) · 02 Sort внутри Pageable · 03 метаданные Page (вся навигация) · 06 Page vs Slice: цена в SQL (2 запроса vs 1, Hibernate Statistics) · 07 граничные PageRequest («за концом», size=0 → исключение, увеличенный size) · 08 нестабильный порядок: тай-брейк по уникальному ключу;
- Нарушений спецификации темы не найдено. Итог по теме: 6 зелёных + 4 красных (ожид.).

## Тема 09 (@Modifying: bulk UPDATE / DELETE) — создана с нуля (2026-11-24)

**Новая тема.** 10 заданий + 10 заранее созданных тестов (пакет `com.example.jpalab.tasks.t09`, каталог `src/test/java/com/example/jpalab/tasks/09-modifying/`) + `tasks/09-modifying/README.md`. Difficulty 1→10, от простого к сложному, без дублей с темами 01–08 (bulk-SQL — отдельный урок от JPA-путей тем 05/06; @Version/оптимистическая блокировка — не в теме).

| # | Diff. | Задание | Статус до реализации | Метод-цель |
|---|---|---|---|---|
| 01 | 1 | Первый bulk UPDATE (цена по категории) | красный | `int updatePricesInCategory(String, BigDecimal)` (новый, ProductRepository) |
| 02 | 2 | Bulk DELETE по имени | красный | `int deleteProductsByName(String)` (новый) |
| 03 | 3 | Конвейер: bulk UPDATE → bulk DELETE | зелёный | существующие `updateStockForExpensive` + `deleteOutOfStock` |
| 04 | 4 | Несколько SET-полей, выражение в правой части | красный | `int raisePricesAndRestock(BigDecimal, BigDecimal)` (новый) |
| 05 | 5 | Коллекционный параметр `IN :names` | красный | `int setStockByNames(List<String>, int)` (новый) |
| 06 | 6 | clearAutomatically: немедленный find возвращает новые данные | красный | `int resetPricesInCategory(String, BigDecimal)` (новый) |
| 07 | 7 | flushAutomatically: запрос видит незаflushенные изменения | зелёный | существующий `updateStockForExpensive` (flushAutomatically = true) |
| 08 | 8 | Ловушка: «устаревшая» managed-сущность переписывает bulk UPDATE | зелёный | существующий `bumpAllPricesBy` (без флагов) |
| 09 | 9 | Ловушка: bulk DELETE обходит JPA cascade (FK) | красный | `int deleteProductsInCategory(String)` (ProductRepository) + `int deleteCategoriesByName(String)` (CategoryRepository) (новые) |
| 10 | 10 | Оба флага в реальной транзакции | красный | `int applyStockPenaltyInCategory(String, int)` (новый; `@SpringBootTest` + `TransactionTemplate`) |

- Все 10: AssertJ, минимальные данные, реальное состояние БД — через native-SELECT (мимо PC); задание 10 — реальные транзакции (setup → рабочая → проверка commit-состояния, `@AfterEach`-очистка).
- **Корректность тестов проверена эмпирически:** временно добавлены 7 новых методов в репозитории → все 10 тестов зелёные (`Tests run: 10, Failures: 0`) → временные методы удалены (git checkout) → 7 красных + 3 зелёных ровно по дизайну. В задании 03 поправлена арифметика (порог 25 vs цена 20 → цены 10/30/40).
- Исключение задания 09 зафиксировано опытом: `org.springframework.dao.DataIntegrityViolationException` (Spring транслирует Hibernate `ConstraintViolationException` на границе репозитория — bulk DELETE выполняется внутри метода репозитория).
- В `tasks/09-modifying/` решений нет (TODO + требуемая сигнатура), `answers/` не создавался, в других темах файлы не трогались.
- Итог по теме: 7 красных (ожид.) + 3 зелёных (03, 07, 08).

## Тема 10 (Проекции) — создана с нуля (2026-11-24)

**Новая тема.** 10 заданий + 10 заранее созданных тестов (пакет `com.example.jpalab.tasks.t10`, каталог `src/test/java/com/example/jpalab/tasks/10-projections/`) + `tasks/10-projections/README.md`. Difficulty 1→10, от простого к сложному. Четыре формы проекций по спеце — open interface, record, класс с конструктором, скалярные поля (Object[]/List) — плюс проекции с JOIN/LEFT JOIN. Без дублей: базовые формы скалярных проекций (1 поле, кортеж 3 колонки) уже в теме 02 — здесь углубление (свой порядок/границы, JOIN-поля, выбор DTO-формы).

| # | Diff. | Задание | Форма | Метод-цель |
|---|---|---|---|---|
| 01 | 1 | Скалярный кортеж (name, price), цена desc + тай-брейк id | `Object[]` | `List<Object[]> namesAndPricesAbove(BigDecimal)` (новый) |
| 02 | 2 | Open interface через @Query (существующий `ProductSummary`) | интерфейс | `List<ProductSummary> summariesInStockAtLeast(int)` (новый) |
| 03 | 3 | Record через конструкторное выражение (существующий `ProductNamePrice`) | record | `List<ProductNamePrice> cheapestInStock(int)` (новый) |
| 04 | 4 | Класс с 5-конструктором, поле из JOIN (`p.category.id`), null-категория | класс `ProductDto` | `List<ProductDto> fullDtosForPricedAtLeast(BigDecimal)` (новый) |
| 05 | 5 | Проекция с JOIN: товар + имя категории (новый интерфейс `ProductCategoryView`) | интерфейс | `List<ProductCategoryView> withCategoryName(String)` (новый + DTO) |
| 06 | 6 | JOIN + сортировка по полю категории (новый record `PricedInCategory`) | record | `List<PricedInCategory> pricedInCategories(BigDecimal)` (новый + DTO) |
| 07 | 7 | LEFT JOIN: товары без категории, null в проекции (новый интерфейс `ProductNameCategory`) | интерфейс | `List<ProductNameCategory> namesWithOptionalCategory()` (новый + DTO) |
| 08 | 8 | Ловушка: порядок аргументов конструкторного выражения (новый record `PriceStockAndName` — price, stock, name) | record | `List<PriceStockAndName> priceStockNameForPricedBelow(BigDecimal)` (новый + DTO) |
| 09 | 9 | JOIN + фильтр по категории + BETWEEN, 4 поля (новый интерфейс `CategoryProductPrice`) | интерфейс | `List<CategoryProductPrice> categoryProductPrices(String, BigDecimal, BigDecimal)` (новый + DTO) |
| 10 | 10 | Один запрос в двух формах: record против нового интерфейса `NamePriceView` | record + интерфейс | `namePricePairsForPricedAtLeast(BigDecimal)` + `namePricePairsAsInterface(BigDecimal)` (новые + DTO) |

- Все 10 красные до реализации (новые методы; DTO из заданий 05–10 студент создаёт сам в `com.example.jpalab.dto` — тесты рефлексивно проверяют FQCN/типы/значения, поэтому модуль компилируется до реализации).
- **Ключевое поведение зафиксировано эмпирически (дебаг Tuple-элементов Hibernate):** open interface projection через `@Query` сопоставляет столбцы с getter'ами по АЛИАСам HQL — столбец без алиаса получает имя `null`, и свойство остаётся null. Поэтому во всех интерфейс-заданиях в SELECT алиасы на каждый столбец (`AS name`, `AS productName`, …); в задании 02 это подчёркнуто в тексте.
- **Корректность проверена:** временно добавлены 11 методов репозитория + 6 DTO → все 10 тестов зелёные (`Tests run: 10, Failures: 0`) → временные DTO и методы удалены (git checkout) → 10 красных ровно с сообщением «Задание не реализовано».
- Проверено опытом: конструкторное выражение с `p.category.id` НЕ выбрасывает товар без категории (INNER-join по полю в SELECT — 4/4 строки, categoryId = null — задание 04); LEFT JOIN сохраняет null-категорию (07); порядок компонентов record vs аргументов `SELECT new` — номинальная проверка селекторами (08).
- В `tasks/10-projections/` решений нет (TODO + сигнатура + контракт DTO), `answers/` не создавался, в других темах файлы не трогались.
- Итог по теме: 10 красных (ожид.), 0 зелёных.

## Тема 11 (Жизненный цикл Entity) — создана с нуля (2026-11-24)

**Новая тема.** 10 заданий + 10 заранее созданных тестов (пакет `com.example.jpalab.tasks.t11`, каталог `src/test/java/com/example/jpalab/tasks/11-lifecycle/`) + `tasks/11-lifecycle/README.md`. Difficulty 1→10, от простого к сложному. По правилу `PROJECT_SPEC.md` (жизненный цикл / транзакции / dirty checking): `@SpringBootTest` + `TransactionTemplate`, не `@DataJpaTest`. Студент **добавляет методы в СУЩЕСТВУЮЩИЙ** класс `com.example.jpalab.service.ProductLifecycleService` (`@Service`, бин `productLifecycleService`; в нём уже есть демонстрационные методы `restock`/`updatePrice` — их не трогают); сервис внедряет `EntityManager` + `PlatformTransactionManager`; **каждый метод работает в своей `REQUIRES_NEW`-транзакции** — это контракт, чтобы тестовая транзакция (H2 READ COMMITTED) видела коммиты сервиса как отдельные. Тесты берут бин по имени из `ApplicationContext` и вызывают методы рефлексивно через `TaskTestSupport.invoke` (модуль компилируется до реализации; метода нет → красный с сообщением «Задание не реализовано: у целевого объекта нет метода ...»). Проверяемое состояние — native-запросами (мимо L1-кэша PC); `@AfterEach`-очистка `deleteAll`.

| # | Diff. | Задание | Метод-цель (сервис) | Что проверяет тест |
|---|---|---|---|---|
| 01 | 1 | Transient → Persistent: persist и commit | `Long persistProduct(String, BigDecimal, int)` | id не null (IDENTITY проставляет при persist), после commit ровно 1 строка, `version = 0` |
| 02 | 2 | Persistent существует только внутри транзакции | `boolean[] containsInsideAndAfterTransaction(Long)` | `[true, false]` — `em.contains` внутри транзакции сервиса / после её commit |
| 03 | 3 | save() на новом экземпляре: маршрутизация в persist | `Object[] saveNewProduct(String, BigDecimal, int)` | `[id, true]` — id присвоен; входной экземпляр НЕ изменён (его id не трогался) |
| 04 | 4 | merge() на transient-инстанции: INSERT, новая managed-копия | `Object[] mergeTransientProduct(String, BigDecimal, int)` | `[managedId, true, true]` — новая managed-копия; входной экземпляр не изменён, его id остаётся null |
| 05 | 5 | Dirty checking: UPDATE без явного save | `void updatePriceByDirtyChecking(Long, BigDecimal)` | цена в БД обновлена, `version` 0→1, UPDATE выполнен при flush |
| 06 | 6 | Identity map в одной транзакции: одна инстанция | `boolean sameInstanceForEntityManagerAndRepository(Long)` | `true` — `em.find` и `repository.findById` в одной транзакции дают ОДНУ и ту же инстанцию |
| 07 | 7 | Identity map в разных транзакциях: разные инстанции | `boolean sameInstanceAcrossTransactions(Long)` | `false` — у каждой транзакции свой persistence context |
| 08 | 8 | em.clear(): новый identity map внутри транзакции | `Object[] identityMapAfterClear(Long)` | `[true, true]` — до clear та же инстанция, после clear — новая загрузка |
| 09 | 9 | Rollback: id присвоен, строки нет | `Long[] rollbackInsertProduct(String, BigDecimal, int)` | `[id ≠ null]` (IDENTITY — id присвоен до rollback), count строк в БД = 0 |
| 10 | 10 | Полный прогон жизненного цикла | `Map<String, Object> fullLifecycleWalk(String, BigDecimal, int)` | `id` не null; `versionAfterDirty == 1`; `priceAfterDetachedMutation == price + 10.00` (dirty +10 в БД, detached +20 без merge — потеряно); `existsAfterRemove == false`; `findableAfterRemove == false` |

- Скоуп темы по спеце покрыт: Transient, Persistent, Detached, Removed, dirty checking, save vs persist vs merge, транзакции и identity map.
- Без дублей: тема 05/09 (merge **отстёгнутого** товара с **новой категорией** — угол FK-связи) ↔ задание 04 здесь (merge **transient**-инстанции — угол идентичности инстанций: вход не мутируется, новая managed-копия); тема 09 (bulk-SQL) — здесь только JPA-пути; `EntityLifecycleExampleTest` (демо) — тесты фиксируют другие свойства (contains, identity map, clear, id после rollback, полный цикл).
- **Корректность тестов проверена эмпирически:** временная реализация сервиса (все 10 методов, REQUIRES_NEW) → все 10 тестов зелёные (`Tests run: 10, Failures: 0`) → временная версия удалена, восстановлен оригинальный файл сервиса из git (с методами `restock`/`updatePrice`) → 10 красных ровно с сообщением «Задание не реализовано: у целевого объекта нет метода ...».
- Важно: класс `ProductLifecycleService` — ОРИГИНАЛЬНЫЙ файл проекта (был в первом коммите); тема формулируется как «добавить методы в существующий сервис», а не «создать класс». Сообщение `TaskTestSupport.invoke` сделано нейтральным («у целевого объекта нет метода») — работает и для репозиториев (темы 01–10), и для сервиса (тема 11).
- В процессе верификации найден и исправлен баг ВЕРИФИКАЦИОННОГО кода (не теста): во временном сервисе `existsAfterRemove` считался как `count == 0` (инвертированное булево). Тесты корректны: print-отладка подтвердила `row=[710.00, 1]` до remove и `count=0` после.
- В `tasks/11-lifecycle/` решений нет (TODO + требуемая сигнатура), `answers/` не создавался, файлы других тем не трогались.
- Итог по теме: 10 красных (ожид.), 0 зелёных.

## Итог `mvn test` (после создания темы 11, 2026-11-24)

```
Tests run: 144, Failures: 69, Errors: 0, Skipped: 0
```

- 34 example-теста — все зелёные.
- 41 task-тест — зелёные (наблюдательные, темы 01–09).
- 69 task-тестов — красные **как задумано**: `AssertionError("Задание не реализовано: ...")` (42 из тем 01–08 + 7 из темы 09 + 10 из темы 10 + 10 из темы 11); 0 ошибок, 0 неожиданных сбоев (все 69 проверены по сообщению).

## Текущая задача

Тема 11 (Жизненный цикл Entity): создана с нуля — 10 `TASK-XX.md` + README + 10 готовых тестов (все 10 красные до реализации), корректность тестов проверена временной реализацией. Темы 01–10 не трогались.

Все 11 тем (`01`–`11`): 110/110 заданий имеют готовый тест.

## Следующий шаг

1. Студент реализовывает методы по `TASK-XX.md` — 69 красных тестов станут зелёными. Проверка: команда из задания или полный `mvn test`.
2. Тема 11: студент добавляет методы в существующий `com.example.jpalab.service.ProductLifecycleService` по контракту в `tasks/11-lifecycle/README.md` (каждый метод — своя REQUIRES_NEW-транзакция).
3. Коммит и push: изменения темы 04 (4 файла), темы 09 (21 новый файл), темы 10 (21 новый файл), темы 11 (21 новый файл) + `PROGRESS.md`.
4. Тема 12 — при отдельном запросе.
