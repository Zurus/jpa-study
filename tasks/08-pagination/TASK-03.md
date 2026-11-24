# Задание 03. Метаданные Page
- Тема: Пагинация (Page / Slice / Pageable / Sort)
- Difficulty: 3
- Что нужно сделать: Тест создан заранее (TASK03Test): 5 товаров, `pageSize = 2`. Проверяются ВСЕ метаданные: `totalPages`, `number`, `size`, `numberOfElements`, `hasNext`/`hasPrevious` для страниц 0, 1 и 2, а также `isEmpty()`/`isFirst()`/`isLast()` и поведение для запроса «за концом» страниц. Методы в репозиторий добавлять не нужно.
- Стартовый код:
```java
// Тест создан заранее: com.example.jpalab.tasks.t08.TASK03Test
// Страницы size = 2 на 5 товарах:
//   p0: number=0, isFirst, !hasPrevious, hasNext, numberOfElements=2
//   p1: number=1, !isFirst, hasPrevious, hasNext, numberOfElements=2
//   p2: number=2, isLast, !hasNext, numberOfElements=1
//   totalPages=3, size=2, totalElements=5
//   PageRequest.of(5, 2): isEmpty()
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t08.TASK03Test#pageMetadataCoversWholeNavigation
```
- Критерий приёмки: Тест: все перечисленные метаданные верифицированы ассертами (включая `totalPages == 3` и последнюю страницу с 1 элементом); запрос страницы 5 (за концом) даёт `isEmpty() == true` при прежних `totalElements`/`totalPages`; в комментарии: из метаданных фронтенд строит «навигацию» без лишних запросов.
- Подсказка (не решение): `Page` — это `List` + `Pageable` + счётчики; каждый геттер метаданных — часть контракта.
