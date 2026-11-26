# jpa-lab

Лабораторная работа: Spring Boot 3.2 + Spring Data JPA + Hibernate (H2, in-memory).
Практическая часть курса JavaRush «SQL & Hibernate»: Spring Data JPA / Hibernate.

## Что внутри

- Сущности: `Product` (с `@Version`, Auditing, Bean Validation), `Category`, `Order`.
- Репозитории, сервисы, DTO.
- 16 тем и 160 заданий — каждое задание проверяется заранее созданным unit-тестом.
- Документация: `PROJECT_SPEC.md` (спецификация), `PROGRESS.md` (ход работы).

## Темы

| Тема | Задания | Тесты | Difficulty |
|---|---:|---:|---|
| 01. Derived queries | 10 | 10 | 1–10 |
| 02. @Query JPQL | 10 | 10 | 1–10 |
| 03. @Query native SQL | 10 | 10 | 1–10 |
| 04. GROUP BY / HAVING | 10 | 10 | 1–10 |
| 05. @ManyToOne + @JoinColumn | 10 | 10 | 1–10 |
| 06. @OneToMany + mappedBy | 10 | 10 | 1–10 |
| 07. N+1 | 10 | 10 | 1–10 |
| 08. Пагинация | 10 | 10 | 1–10 |
| 09. @Modifying | 10 | 10 | 1–10 |
| 10. Проекции | 10 | 10 | 1–10 |
| 11. Жизненный цикл Entity | 10 | 10 | 1–10 |
| 12. Нюансы | 10 | 10 | 1–10 |

## Как проходить задания

1. Открой `TASK-XX.md` (в папке `tasks/<тема>/`; общий контекст темы — в `README.md` этой папки).
2. Реализуй задание в `src/main/java`.
3. Запусти указанный в задании тест:
   `mvn -Dtest=<полное имя тестового класса>#<имя метода>`
4. Тест должен пройти.

## Решения

Решения в виде markdown-файлов не создаются.
Каждое задание проверяется готовым unit-тестом.

## Следующие шаги

- GROUP BY / HAVING через Criteria API
- Specification
- Second-level cache
- Продвинутый маппинг типов
- Наследование сущностей
- Полный маппинг коллекций
