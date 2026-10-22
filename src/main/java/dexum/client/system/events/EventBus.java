package dexum.client.system.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private static class EventListener {
        final Object owner;
        final Method method;
        final EventPriority priority;

        EventListener(Object owner, Method method, EventPriority priority) {
            this.owner = owner;
            this.method = method;
            this.priority = priority;
        }
    }

    private static final Map<Class<? extends Event>, List<EventListener>> listeners =
            new ConcurrentHashMap<>();

    private static final Set<Object> registeredObjects = Collections.newSetFromMap(
            new ConcurrentHashMap<>()
    );

    // Throttling для логирования ошибок
    private static final Map<String, Long> ERROR_THROTTLE = new ConcurrentHashMap<>();
    private static final long ERROR_THROTTLE_MS = 2_500L;

    public static void register(Object object) {
        if (registeredObjects.contains(object)) return;

        registeredObjects.add(object);

        for (Method method : object.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class)) continue;
            if (method.getParameterCount() != 1) continue;

            Class<?> paramType = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(paramType)) continue;

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass = (Class<? extends Event>) paramType;

            EventHandler annotation = method.getAnnotation(EventHandler.class);
            EventPriority priority = annotation.priority();

            method.setAccessible(true);

            listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                    .add(new EventListener(object, method, priority));

            listeners.get(eventClass).sort(
                    Comparator.comparingInt(l -> l.priority.getLevel())
            );
        }
    }

    public static void unregister(Object object) {
        if (!registeredObjects.contains(object)) return;

        registeredObjects.remove(object);

        for (List<EventListener> listenerList : listeners.values()) {
            listenerList.removeIf(listener -> listener.owner == object);
        }
    }

    public static <T extends Event> T post(T event) {
        List<EventListener> eventListeners = listeners.get(event.getClass());

        if (eventListeners == null || eventListeners.isEmpty()) {
            return event;
        }

        // Поддержка EventStoppable - прерывает обработку при вызове stop()
        if (event instanceof EventStoppable stoppable) {
            for (EventListener listener : eventListeners) {
                invoke(listener, event);
                
                if (stoppable.isStopped()) {
                    break;
                }
            }
        } 
        // Поддержка EventCancellable - если отменено, пропускаем LOW/NORMAL, вызываем только HIGH+
        else if (event instanceof EventCancellable cancellable) {
            for (EventListener listener : eventListeners) {
                if (cancellable.isCancelled()) {
                    // Пропускаем слушателей ниже HIGH
                    if (listener.priority.getLevel() < EventPriority.HIGH.getLevel()) {
                        continue;
                    }
                }
                invoke(listener, event);
            }
        } 
        // Обычные события
        else {
            for (EventListener listener : eventListeners) {
                invoke(listener, event);
            }
        }

        return event;
    }

    /**
     * Вызов метода-обработчика с улучшенной обработкой ошибок
     */
    private static void invoke(EventListener listener, Event event) {
        try {
            listener.method.invoke(listener.owner, event);
        } catch (IllegalAccessException e) {
            logInvocationError(listener, event, e);
        } catch (IllegalArgumentException e) {
            logInvocationError(listener, event, e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            logInvocationError(listener, event, cause);
        }
    }

    /**
     * Логирование ошибок с throttling для предотвращения спама
     */
    private static void logInvocationError(EventListener listener, Event event, Throwable throwable) {
        String key = listener.owner.getClass().getName() + "#" + listener.method.getName() + ":" + throwable.getClass().getName();
        long now = System.currentTimeMillis();
        Long last = ERROR_THROTTLE.get(key);

        // Пропускаем если ошибка уже была залогирована недавно
        if (last != null && now - last < ERROR_THROTTLE_MS) {
            return;
        }

        ERROR_THROTTLE.put(key, now);

        String message = "[" + now + "] Event handler failed: "
                + listener.owner.getClass().getName()
                + "#" + listener.method.getName()
                + "(" + event.getClass().getSimpleName() + ") -> "
                + throwable.getClass().getName()
                + ": " + String.valueOf(throwable.getMessage());

        // Пытаемся записать в файл
        try {
            Path logFile = Path.of("dexum-event-errors.log");
            Files.writeString(
                    logFile,
                    message + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (Exception ignored) {
            // Если не удалось записать в файл, выводим в консоль
            System.err.println(message);
            throwable.printStackTrace();
        }
    }
    public static boolean isRegistered(Object object) {
        return registeredObjects.contains(object);
    }

    public static void clear() {
        listeners.clear();
        registeredObjects.clear();
    }

    public static int getListenerCount(Class<? extends Event> eventClass) {
        List<EventListener> list = listeners.get(eventClass);
        return list == null ? 0 : list.size();
    }
}
