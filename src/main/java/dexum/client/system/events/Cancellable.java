package dexum.client.system.events;

/**
 * Интерфейс для событий, которые могут быть отменены
 */
public interface Cancellable {
    
    /**
     * Проверяет, было ли событие отменено
     */
    boolean isCancelled();
    
    /**
     * Отменяет событие
     */
    void cancel();
    
    /**
     * Устанавливает статус отмены события
     */
    void setCancelled(boolean cancelled);
}
