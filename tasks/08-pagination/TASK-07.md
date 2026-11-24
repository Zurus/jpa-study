# Задание 07. Граничные PageRequest: «за концом» и size = 0
- Тема: Пагинация (Page / Slice / Pageable / Sort)
- Difficulty: 7
- Что нужно сделать: Тест создан заранее (TASK07Test): 5 товаров и три граничных вызова: (а) `PageRequest.of(100, 2)` — страница далеко за концом; (б) `PageRequest.of(0, 0)` — нулевой размер страницы; (в) `PageRequest.of(0, 5)` — размер больше общего числа элементов. Зафиксировано поведение каждого. Методы в репозиторий добавлять не нужно.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t08.TASK07Test
// (a) of(100, 2): content пусто, totalElements 5, totalPages 3 — без ошибки
// (b) of(0, 0):   assertThrows(IllegalArgumentException) — size обязателен > 0
// (c) of(0, 5):   все 5 элементов, totalPages 1, hasNext false
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t08.TASK07Test#edgeCasesBeyondEndZeroSizeOversized
```
- Критерий приёмки: Тест: (a) пустой content при корректных метаданных (метод не «падает» на несуществующей странице); (b) нулевой size отклонён на валидации `PageRequest` — `assertThrows(IllegalArgumentException.class)`; (c) «одна большая страница» — все элементы, `totalPages == 1`; в комментарии: почему API-слою нужны обе защиты (валидация size на входе + корректное поведение «за концом»).
- Подсказка (не решение): `PageRequest` валидирует `size > 0` при создании, а «за конец» — валидное, но пустое состояние.
