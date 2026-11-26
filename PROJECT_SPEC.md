# JPA Lab — спецификация

Практическая часть курса JavaRush «SQL & Hibernate»: Spring Data JPA / Hibernate.

## Технологии
- Java 17
- Spring Boot 3.2
- Maven
- Spring Data JPA + Hibernate 6
- H2 (in-memory, READ COMMITTED)
- Lombok
- JUnit 5
- AssertJ
- Hibernate Statistics (подсчёт SQL-запросов в тестах)
- spring-boot-starter-validation (Bean Validation)

## Сущности (стартовый код, не менять структуру)
- `Product`: id (IDENTITY), name, price, stockQuantity, createdAt, updatedAt, version (`@Version`), category (`@ManyToOne @JoinColumn @Lazy`)
- `Category`: id, name, products (`@OneToMany(mappedBy = "category", cascade = ALL, orphanRemoval = true)`)
- `Order`: id, orderDate, product (`@ManyToOne`)
- DTO: `ProductSummary` (open interface в `ProductRepository`), `ProductNamePrice` (record), `ProductDto` (класс с конструктором)

## Принцип обучения
Модель «красный до реализации — зелёный после»:
1. Каждое задание проверяется **заранее созданным** фиксированным unit-тестом (один тестовый класс на задание, имя `TASK<XX>Test`).
2. Студент реализует решение в `src/main/java` (репозитории / сервисы / сущности / DTO) — строго по «Что нужно сделать» и сигнатурам из `TASK-XX.md`.
3. До реализации тест **красный** (отсутствие метода студента → `AssertionError` «Задание не реализовано» через `TaskTestSupport.invoke`, либо провал поведенческой assertion).
4. После корректной реализации тест **зелёный**. Зелёный тест на базовой линии (без кода студента) — дефект задания.
5. Тест создаётся ДО студента и не меняется под решение студента.

## Правила тестов
- Репозитории и JPQL/native-запросы: `@DataJpaTest`.
- Commit/rollback, жизненный цикл, identity map, `@Version`-гонки, bulk в реальной транзакции: `@SpringBootTest` + `TransactionTemplate` (реальные транзакции, H2 READ COMMITTED).
- Сервисные контракты: `@Service` с фиксированным именем бина; тест берёт бин через `applicationContext.getBean("...")` и вызывает метод рефлексивно (`TaskTestSupport.invoke`) — модуль компилируется до реализации, отсутствие метода даёт красный тест с понятным сообщением.
- Для тем про `@Service` в `@DataJpaTest`-тестах — `@Import(Служба.class)`.
- AssertJ для всех assertions; свои минимальные данные в каждом тесте (без общих фикстур); реальное состояние БД — через native-SELECT (мимо L1-кэша).
- N+1 / пагинация: точный подсчёт prepared statements через Hibernate Statistics (`getPrepareStatementCount`, `clear()` перед замером).
- `@Modifying`: проверка изменения данных, состояния PC (clearAutomatically/flushAutomatically) и числа затронутых строк.
- `@Version`: `OptimisticLockingFailureException` при конкурентном обновлении через `productRepository.save` (граница Spring Data); JPA `OptimisticLockException` — через `em.merge`.
- **Тесты не раскрывают решение**: в тесте и в `TASK-XX.md` нет текста готового запроса/реализации; «Стартовый код» — `// TODO` + требуемая сигнатура; «Подсказка» — одна строка без готового выражения.
- Структура тестов: каталоги `src/test/java/com/example/jpalab/tasks/<NN>-<тема>/`, пакеты `com.example.jpalab.tasks.t01`…`t13`. Каталог соответствует теме, пакет — теме; сверка задания с тестом идёт по каталогу темы (имя класса `TASK01Test` совпадает во всех темах).
- Общий помощник: `TaskTestSupport.invoke(repository|service, methodName, Class<?>[] parameterTypes, Object... args)` — рефлексивный вызов метода студента; нет метода → `AssertionError` «Задание не реализовано: …». `TaskTestSupport.newInstance(className)` — рефлексивное создание класса-артефакта студента по FQCN (тема 13: `ProductSpecifications`); нет класса → `AssertionError` «Задание не реализовано».
- `answers/` не создаётся; решений в `tasks/`, `src/`, тестах нет.

## Формат TASK-XX.md
```markdown
# Задание XX. <название>
- Тема: <тема>
- Difficulty: <1–10>
- Что нужно сделать: <1–3 конкретных предложения: где, какой метод, какое поведение>
- Стартовый код:
```java
// TODO: <что делать>
<сигнатура метода>
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.tNN.TASKXXTest#<имя метода теста>
```
- Критерий приёмки: <тест + поведение (SQL-лог/исключения, где требуется)>
- Подсказка (не решение): <одна строка>
```

## Правила генерации
- На каждую тему ровно 10 заданий (TASK-01.md … TASK-10.md).
- Каждому заданию — ровно один заранее созданный тестовый класс `TASKXXTest` (один `@Test`-метод).
- Difficulty 1–10, строго возрастает внутри темы: 1–3 — минимальное использование конструкции; 4–7 — комбинации; 8–10 — NULL, LAZY, N+1, кэш, транзакции, граничные случаи.
- Задания не дублируются внутри темы и между темами (см. «Границы тем»).
- Тест зелёный до реализации — запрещён: задание обязано требовать артефакт студента (метод репозитория/сервиса/сущности/DTO).
- Тест самодостаточен: создаёт данные, вызывает целевой метод студента, проверяет результат и (где требуется) SQL-лог.
- `answers/` не создаётся, markdown-решения не пишутся.

## Темы: текущий скоуп (01–14, 10 заданий каждая)

### 01. Derived queries (вывод запроса из имени метода)
Предикаты (GreaterThan, Between, Containing, IgnoreCase, IsNull/IsNotNull), коннекторы And/Or, Top/First/Last, OrderBy, Sort, свойство-путь через ассоциацию (`CategoryName`), Pageable, NULL, N+1 у derived + LAZY, порядок параметров. Только имена методов — без `@Query`.

### 02. JPQL @Query
Именованные параметры (`@Param`), функции (UPPER/LOWER/CONCAT), LIKE, явные JOIN, implicit-join vs explicit, подзапрос IN, выборка одного поля/`Object[]`, JPQL+Pageable, свойство сущности vs колонка БД, L1-кэш и managed-результат. Без constructor expression (это тема 10), без Specification/Criteria/Querydsl.

### 03. Native queries
`nativeQuery = true`, snake_case-имена таблиц/колонок, авто-маппинг (сущность по `*`, `Object[]`, скаляр), JOIN в SQL, JDBC-пагинация (`setFirstResult`/`setMaxResults`), ограничение: constructor expression в native не работает, NULL в native, managed-результат, LAZY-ассоциации без fetch.

### 04. GROUP BY / HAVING
COUNT/SUM/AVG/MIN/MAX, GROUP BY (включая через ассоциацию), HAVING, LEFT JOIN и пустые группы, NULL-семантика агрегатов, алиас агрегата в HAVING (ловушка), Page по сгруппированному запросу (считает группы).

### 05. @ManyToOne (Product → Category)
Владелец FK, LAZY-прокси, identity map, dirty checking (FK + поле в одном UPDATE), transient-категория без cascade, FK-ограничение БД, удаление «одного» при живых ссылках, merge detached со сменой категории, rollback FK. Методы-артефакты — в `ProductLifecycleService`.

### 06. @OneToMany (Category → products)
Каскад PERSIST/MERGE коллекции, managed-коллекция, orphanRemoval, перенос между категориями, коллекция без обратной ссылки (FK не двигается), stale-коллекция + refresh, каскад REMOVE (дети → родитель), «двойное владение», N INSERT без батчинга, rollback каскадных INSERT-ов. Методы-артефакты — в `ProductLifecycleService`.

### 07. N+1 и стратегии загрузки (JOIN FETCH / @EntityGraph / batch fetching)
Измерение 1+N через Hibernate Statistics, JOIN FETCH (to-one и с WHERE/ORDER BY), @EntityGraph, batch fetching (`default_batch_fetch_size`), fetch join + пагинация, «мультипликация» строк в count-запросе, сервисный сценарий. L1-кэш здесь — только контекст, отдельной темой кэширования не является.

### 08. Пагинация (Page / Slice / Pageable / Sort)
Page (count + данные, 2 SQL) vs Slice (hasNext, «лишняя» строка, 1 SQL), метаданные Page, PageRequest («за концом», size=0), Sort и тай-брейки по уникальному ключу, пагинация + фильтрация + сортировка, N+1 на странице и лечение @EntityGraph. Чистая фильтрация без Pageable — не тема 08 (это 01–04).

### 09. @Modifying
Bulk UPDATE/DELETE через `@Query`, число затронутых строк, несколько SET-полей и выражения в правой части, `IN :names`, flushAutomatically, clearAutomatically, «устаревшая» сущность переписывает bulk UPDATE, bulk DELETE обходит JPA cascade (FK), оба флага в реальной транзакции. Без @Version-сценариев (это тема 12).

### 10. Проекции
Четыре формы: open interface, record (constructor expression), класс с конструктором, скалярные `Object[]`; проекции с JOIN / LEFT JOIN (null-категория), сортировка по полям JOIN, порядок аргументов конструкторного выражения, один запрос в двух формах. Новые DTO студент создаёт сам в `com.example.jpalab.dto`.

### 11. Жизненный цикл Entity
Transient/Persistent/Detached/Removed, persist vs save vs merge, dirty checking, identity map (одна транзакция — одна инстанция; разные транзакции — разные), `em.clear()`, rollback и IDENTITY-ключ. Актор — сервис `ProductLifecycleService` (каждый метод — REQUIRES_NEW); тесты `@SpringBootTest` + `TransactionTemplate`. Транзакционные свойства (уровни изоляции, savepoints, матрица propagation) — не тема 11 (плановая тема 19).

### 12. Нюансы (Bean Validation / Auditing / @Version)
Bean Validation на flush (не на setter), `@Size` на сущностях, Auditing (`@CreatedDate`/`@LastModifiedDate`, «пустой» flush не двигает поля), `@Version` (JPA-UPDATE поднимает version), оптимистичная блокировка (`OptimisticLockingFailureException` через repository.save, retry-паттерн), bulk `@Modifying` не трогает version. Без pessimistic locking `@Lock` (плановая тема 20), без soft delete (плановая тема 24), без second-level cache (плановая тема 21).

### 13. Specification: динамические предикаты
`Specification<Product>` и `JpaSpecificationExecutor` (добавлен в `ProductRepository` в стартовом коде): первый предикат (eq), comparison (`>=`), динамический фильтр (null — «всегда верен»), композиция `and`/`or`/`not`, `Specification.where`, переиспользуемые спецификации, предикат по свойству-пути через ассоциацию (`category.name`, implicit-join), ловушка приоритета `and`/`or` (явная группировка), полный динамический фильтр (открытый диапазон цены + категория + остаток, NULL-категория), Specification + Pageable. Артефакт — класс `com.example.jpalab.repository.ProductSpecifications` (статические методы, публичный no-arg конструктор); тесты находят класс/методы рефлексивно (`TaskTestSupport.newInstance` + `invoke`). Только Specification: без прямого Criteria API (тема 14) и Querydsl; статические фильтры — 01–04, чистая пагинация — 08 (здесь только в сочетании со Specification).

### 14. Criteria API: программные запросы
Программная сборка запроса без строкового JPQL: `CriteriaBuilder`, `CriteriaQuery<T>`, `Root`, `Path`, `Predicate`, `Join`; eq/диапазон/`in`-предикаты; выборка `Path` (скаляр `CriteriaQuery<String>`); `Join` (inner) и `JoinType.LEFT` с NULL-семантикой (товар без категории); сортировка `CriteriaQuery.orderBy` (тай-брейк при равных значениях); программная сборка динамического WHERE (условные предикаты, список имён); агрегаты `cb.sum` (NULL по пустой группе) и `cb.avg` (тип `Expression<Double>`); подзапрос `subquery` («категории с дорогим товаром»); GROUP BY + HAVING + orderBy по группе (аналог запроса темы 04 в программной форме — разрешённое исключение). Артефакт — методы сервиса `com.example.jpalab.service.ProductCriteriaService` (скелет сервиса с `EntityManager` в стартовом коде; тесты через `@Import` + `TaskTestSupport.invoke`). Только Criteria API на прямую: без Specification (тема 13) и Querydsl (тема 18); логические запросы тем 01–04 разрешено повторять в форме Criteria; чистая пагинация — тема 08.

## Планируемые темы (15–25) — ВНЕ текущего скоупа
Задания и тесты для этих тем в текущем релизе НЕ создаются (скоуп — ровно 140 заданий тем 01–14). Перечень плановый, порядок может меняться:

- 15. Наследование сущностей (SINGLE_TABLE / JOINED / TABLE_PER_CLASS, @DiscriminatorColumn)
- 16. Продвинутый маппинг типов (@Embedded, @Convert/@Converter, EnumType, составные ключи)
- 17. Полный маппинг коллекций (@ElementCollection, @CollectionTable, @MapKey*, @OrderColumn)
- 18. Querydsl (типобезопасный DSL)
- 19. Транзакции в JPA (уровни изоляции, propagation, savepoints, длинные транзакции)
- 20. Пессимистичная блокировка (@Lock PESSIMISTIC_READ/PESSIMISTIC_WRITE, lock timeout)
- 21. Second-level cache (региональные кэши, @Cacheable, провайдеры)
- 22. Query cache и стратегии кэширования
- 23. Hibernate Event Listeners (@PrePersist/@PostLoad/… и их порядок)
- 24. Soft delete (soft-delete паттерн, фильтрация удалённых, ограничения)
- 25. Производительность: батчинг, fetch-стратегии, N+1 в сложных графах

## Границы тем (антидубли)
- Темы 01–04: только derived / JPQL / native / GROUP BY. **Запрещены** Specification, Criteria API, Querydsl (темы 13, 14, 18).
- Тема 07 (N+1) **не дублирует** темы кэширования: first-level cache упоминается только как контекст измерения, second-level cache — вообще отсутствует; кэширование — плановые темы 21/22.
- Тема 08 (пагинация) **не дублирует** обычные фильтрации тем 01–04: каждое задание 08 обязательно использует Pageable/Slice/Sort; «просто фильтр» здесь не задаётся.
- Тема 11 (жизненный цикл) **не дублирует** сценарии транзакций плановой темы 19: здесь — состояния, identity map и REQUIRES_NEW как контракт сервиса; изоляция, savepoints, матрица propagation — только тема 19.
- Тема 12 (нюансы) **не содержит**: pessimistic locking `@Lock` (тема 20), soft delete (тема 24), second-level cache (тема 21).
- Тема 13 (Specification) **не выходит за границы**: только `Specification`/`JpaSpecificationExecutor` — без прямого Criteria API (тема 14) и Querydsl (тема 18); без дублей статических derived queries (01) и JPQL (02); пагинация — только в сочетании Specification + Pageable (чистая пагинация — тема 08).
- Тема 14 (Criteria API) **не выходит за границы**: только API напрямую — без Specification (тема 13) и Querydsl (тема 18); повторение логических запросов тем 01–04 через Criteria разрешено (акцент на API, а не на запросе); чистая пагинация — тема 08; GROUP BY/HAVING — разрешённое исключение темы 04 в программной форме.
- Тема 09 (bulk) и тема 12 не пересекаются: 09 — механика bulk-SQL и PC, 12 — семантика `@Version`/валидации; единственная стыковая точка (bulk не поднимает version) закреплена в задании 12/09.

## Важное (скоуп)
- Проект покрывает практическую Spring Data JPA / Hibernate-часть курса, а не весь курс. За кадром: SQL-основы, JDBC, проектирование БД, кластеры и облачные БД.
- Текущий релиз: ровно 14 тем × 10 заданий = 140 заданий, 140 заранее созданных тестов. Тем 15–25 в репозитории нет и их не создавать без отдельного запроса.
- `answers/` не создаётся; каждое задание проверяется unit-тестом.
