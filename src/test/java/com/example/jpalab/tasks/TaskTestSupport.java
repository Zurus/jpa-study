package com.example.jpalab.tasks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Вспомогательный класс для заранее созданных тестов заданий (темы 01–08).
 *
 * Тесты вызывают метод репозитория по имени и точной сигнатуре.
 * Если студент ещё не добавил метод в src/main/java, тест падает с понятным
 * сообщением об ошибке (а не ломает компиляцию всего модуля),
 * и все остальные тесты продолжают работать.
 */
public final class TaskTestSupport {

    private TaskTestSupport() {
    }

    /**
     * Вызывает публичный метод объекта по имени и типам параметров.
     *
     * @param repository     прокси репозитория (ProductRepository и т.д.)
     * @param methodName     точное имя метода из условия задания
     * @param parameterTypes точные типы параметров из условия задания
     * @param args           аргументы вызова
     * @param <T>            тип результата
     * @return результат вызова
     * @throws AssertionError если метод с такой сигнатурой ещё не реализован
     */
    @SuppressWarnings("unchecked")
    public static <T> T invoke(Object repository, String methodName, Class<?>[] parameterTypes, Object... args) {
        Method method;
        try {
            method = repository.getClass().getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(
                    "Задание не реализовано: в репозитории нет метода " + methodName
                            + " с сигнатурой (" + describe(parameterTypes) + ")."
                            + " Добавьте метод с точно такой сигнатурой в src/main/java"
                            + " (ProductRepository или CategoryRepository, см. условие задания).", e);
        }
        try {
            return (T) method.invoke(repository, args);
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (target instanceof Error error) {
                throw error;
            }
            throw new AssertionError("Метод " + methodName + " выбросил исключение: " + target, target);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Не удалось вызвать метод " + methodName, e);
        }
    }

    private static String describe(Class<?>[] parameterTypes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameterTypes[i].getSimpleName());
        }
        return sb.toString();
    }
}
