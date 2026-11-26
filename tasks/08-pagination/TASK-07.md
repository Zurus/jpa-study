# Задание 07. Граничные PageRequest: «за концом» и size = 0
- Тема: Пагинация (Page / Slice / Pageable / Sort)
- Difficulty: 7
- Что нужно сделать: Добавьте в `ProductService` метод `Page<ProductDto> pageProductsForApi(int page, int size)` — «API-слой»: построить `PageRequest.of(page, size, Sort.by("id"))`, загрузить страницу товаров (`productRepository.findAll`) и вернуть страницу DTO (`ProductDto.fromEntity` по каждому элементу). Заранее созданный тест (TASK07Test) проверяет три граничных случая: (а) страница далеко за концом; (б) нулевой размер; (в) размер больше общего числа элементов.
- Стартовый код:
```java
// com/example/jpalab/service/ProductService.java

// TODO: PageRequest.of(page, size, Sort.by("id")) -> findAll(pageable) ->
//       Page.map(ProductDto::fromEntity)
Page<ProductDto> pageProductsForApi(int page, int size);
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t08.TASK07Test#edgeCasesBeyondEndZeroSizeOversized
```
- Критерий приёмки: Тест: 5 товаров (10…50.00); (a) `pageProductsForApi(100, 2)` — пустой content при корректных метаданных (totalElements 5, totalPages 3, без ошибки); (b) `pageProductsForApi(0, 0)` — `IllegalArgumentException` на валидации `PageRequest` ДО запроса; (c) `pageProductsForApi(0, 5)` — все 5 элементов, totalPages 1, hasNext false, isLast true; в комментарии: почему API-слою нужны обе защиты (валидация size на входе + корректное поведение «за концом»).
- Подсказка (не решение): `PageRequest` валидирует `size > 0` при создании, а «за конец» — валидное, но пустое состояние; `Page.map` превращает страницу сущностей в страницу DTO.
