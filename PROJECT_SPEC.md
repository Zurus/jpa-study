# JPA Lab — спецификация

## Технологии
- Java 17
- Spring Boot 3.2+
- Maven
- Spring Data JPA + Hibernate
- H2
- Lombok
- AssertJ
- JUnit 5

## Сущности
- Product: id, name, price, stockQuantity, createdAt, updatedAt, version, category
- Category: id, name, products
- Order: id, orderDate, product

## Принцип обучения
Каждое задание проверяется готовым unit-тестом.
Студент реализует решение в src/main/java.
Тест создаётся заранее и должен падать до реализации.
После корректной реализации тест должен проходить.

## Правила тестов
- Репозитории и JPQL/native-запросы: @DataJpaTest.
- Жизненный цикл, транзакции, dirty checking, detached-сущности, @Version: @SpringBootTest + TransactionTemplate.
- AssertJ для assertions.
- Для N+1 проверяй количество SQL-запросов или использование JOIN FETCH.
- Для @Modifying проверяй изменение данных и состояние persistence context.
- Для @Version проверяй OptimisticLockingFailureException при конкурентном обновлении.
- Все тесты должны проходить после корректной реализации задания.

## Формат TASK-XX.md
# Задание XX. <название>
- Тема: <тема>
- Difficulty: <1–10>
- Что нужно сделать: <1–3 конкретных предложения>
- Стартовый код:
```java
// TODO
```
- Тест для проверки:
```bash
mvn -Dtest=<полное имя тестового класса>#<имя метода>
```
- Критерий приёмки: <тест должен пройти>
- Подсказка (не решение): <одна строка>

## Правила генерации
- На каждую тему ровно 10 заданий.
- Каждому заданию соответствует ровно один тестовый класс.
- difficulty от 1 до 10.
- 1–3: минимальное использование конструкции.
- 4–7: комбинации.
- 8–10: NULL, LAZY, N+1, кэш, транзакции, граничные случаи.
- Задания не дублируются между темами.
- Решения в answers/ не создаются.
- Тесты не должны содержать готовую реализацию задания.
- Тесты должны быть самодостаточными: создавать данные, вызывать целевой метод и проверять результат.

## Полный список тем

### 01. Derived queries
Derived query methods Spring Data JPA:
SELECT, WHERE, ORDER BY, LIMIT.
Примеры: findByName, findByPriceLessThan, findTop3By...
Не использовать @Query в этой теме.

### 02. @Query JPQL
JPQL через @Query:
параметры, LIKE, сортировка, выборка одного поля.
Не включать constructor expression — это тема 10.

### 03. @Query native SQL
Нативные SQL-запросы:
nativeQuery = true, отличие от JPQL, параметры, скалярные выборки, ограничения.

### 04. GROUP BY / HAVING
JPQL:
GROUP BY, HAVING, COUNT, SUM, AVG, MIN, MAX, группировка по связанной сущности.

### 05. @ManyToOne + @JoinColumn
@ManyToOne, @JoinColumn, LAZY vs EAGER, прокси, SQL, NULL-связи, фильтрация и сортировка по связанной сущности.

### 06. @OneToMany + mappedBy
@OneToMany, mappedBy, cascade, orphanRemoval, владелец связи, добавление и удаление дочерних сущностей.

### 07. N+1
N+1, JOIN FETCH, @EntityGraph, batch fetching, подсчёт SQL-запросов, LAZY-коллекции.

### 08. Пагинация
Page, Slice, Pageable, Sort, PageRequest, пагинация с фильтрацией, отличие Page от Slice.

### 09. @Modifying
UPDATE/DELETE через @Query, @Modifying, flushAutomatically, clearAutomatically, влияние на persistence context.

### 10. Проекции
interface-based projection, record DTO, constructor expression, проекция полей, проекция с JOIN.

### 11. Жизненный цикл Entity
Transient, Persistent, Detached, Removed, dirty checking, save vs persist vs merge, транзакции и identity map.

### 12. Нюансы
Bean Validation, Auditing @CreatedDate/@LastModifiedDate, @Version, оптимистичная блокировка, OptimisticLockingFailureException.

## Важно
Этот проект покрывает практическую Spring Data JPA / Hibernate-часть курса JavaRush «SQL & Hibernate», а не весь курс.
За кадром: SQL-основы, JDBC, продвинутый маппинг типов, наследование, полные коллекции, second-level cache, проектирование БД, кластеры и облачные БД.
