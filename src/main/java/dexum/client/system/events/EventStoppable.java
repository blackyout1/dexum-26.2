package dexum.client.system.events;

/**
 * Базовый класс для событий, которые могут остановить цепочку обработки
 * В отличие от EventCancellable, EventStoppable прерывает дальнейшую обработку события,
 * но не отменяет само действие
 */
public abstract class EventStoppable implements Event {

    private boolean stopped;

    protected EventStoppable() {
        this.stopped = false;
    }

    /**
     * Останавливает дальнейшую обработку события
     */
    public void stop() {
        stopped = true;
    }

    /**
     * Проверяет, была ли остановлена обработка события
     */
    public boolean isStopped() {
        return stopped;
    }
}
