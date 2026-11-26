# Задание 05. Auditing: добавить @CreatedDate в сущность Order
- Тема: Нюансы (Auditing)
- Difficulty: 5
- Что нужно сделать: Включите аудит в сущность `Order` (`src/main/java/com/example/jpalab/entity/Order.java`): (1) добавьте поле `LocalDateTime createdAt` с аннотацией `@CreatedDate` (имя поля должно быть именно `createdAt` — тест проверяет геттер `getCreatedAt()` через рефлексию); (2) добавьте на класс аннотацию `@EntityListeners(AuditingEntityListener.class)`. После этого при сохранении заказа аудитор заполнит поле без нашего кода, а в таблице `orders` появится колонка `created_at`.
- Стартовый код:
```java
// TODO: в src/main/java/com/example/jpalab/entity/Order.java:
// 1) на класс — @EntityListeners(AuditingEntityListener.class);
// 2) поле:
//    @CreatedDate
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
// (импорт: org.springframework.data.annotation.CreatedDate,
//  org.springframework.data.jpa.domain.support.AuditingEntityListener,
//  jakarta.persistence.EntityListeners)
```
- Тест для проверки:
```bash
mvn -Dtest=com.example.jpalab.tasks.t12.TASK05Test#orderCreatedAtFilledByAuditing
```
- Критерий приёмки: Тест: после `orderRepository.save` геттер `getCreatedAt()` возвращает `LocalDateTime` в пределах минуты от текущего времени; native-запрос `select created_at from orders where id = ?` возвращает ненулевое значение.
- Подсказка (не решение): `@EnableJpaAuditing` уже стоит на `JpaLabApplication` — достаточно слушателя на сущности и аннотации на поле; геттер даст Lombok `@Getter` (уже на классе).
