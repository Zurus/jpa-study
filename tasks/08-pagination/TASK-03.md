# Задание 03. Метаданные Page
- Тема: Пагинация (Page / Slice / Pageable / Sort)
- Difficulty: 3
- Что нужно сделать: Добавьте в `ProductRepository` метод `Page<Product> findByNameStartingWithIgnoreCase(@Param("prefix") String prefix, Pageable pageable)` — товары, имя которых НАЧИНАЕТСЯ с `:prefix` БЕЗ учёта регистра (префикс может прийти в любом регистре из API), страницей. Заранее созданный тест (TASK03Test) на 5 таких товарах (плюс 2 товара с другим префиксом) проверяет ВСЕ метаданные: `totalPages`, `number`, `size`, `numberOfElements`, `hasNext`/`hasPrevious`, `isEmpty()`/`isFirst()`/`isLast()` для страниц 0, 1 и 2.
- Стартовый код:
```java
// com/example/jpalab/repository/ProductRepository.java

// TODO: @Query (JPQL) — имя начинается с :prefix без учёта регистра
Page<Product> findByNameStartingWithIgnoreCase(@Param("prefix") String prefix, Pageable pageable);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t08.TASK03Test#pageMetadataCoversWholeNavigation
```
- Критерий приёмки: Тест: 5 товаров Gadget1…Gadget5 (имена в разных регистрах) + 2 Widget'а; запросы с префиксами "GADGET" и "gadget" дают `totalElements == 5`, `totalPages == 3`; p0 — isFirst/hasNext/numberOfElements=2, p1 — hasPrevious/numberOfElements=2, p2 — isLast/numberOfElements=1; в комментарии: из метаданных фронтенд строит «навигацию» без лишних запросов.
- Подсказка (не решение): «без учёта регистра» в JPQL — сравнение нормализованных строк (например, через UPPER с обеих сторон); `Page` — это `List` + `Pageable` + счётчики, каждый геттер — часть контракта.
