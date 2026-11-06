package dexum.client.utils.animation;

/**
 * Helper class for animating between two states (0 and 1).
 * Used for fade-in/fade-out and visibility transitions.
 */
public class AnimationHandler {
    private Animation animation;
    private Direction direction;

    public AnimationHandler(float startValue, float duration) {
        this.animation = new Animation((long)(duration * 1000), startValue, Easing.QUAD_OUT);
        this.direction = Direction.BACKWARDS;
    }

    public void start(Direction direction) {
        this.direction = direction;
        float target = direction == Direction.FORWARDS ? 1.0f : 0.0f;
        animation.update(target);
    }

    /** Двигает анимацию к цели текущего направления. */
    public void tick() {
        float target = direction == Direction.FORWARDS ? 1.0f : 0.0f;
        animation.update(target);
    }

    /**
     * Тикает анимацию и возвращает текущее значение (0..1).
     * Тик здесь — чтобы любой вызов getOutput() двигал анимацию,
     * даже если никто не зовёт {@link #tick()} отдельно.
     * Двойной тик в одном кадре безопасен: {@link Animation#update(float)} идемпотентен.
     */
    public Float getOutput() {
        tick();
        return animation.getValue();
    }

    public boolean isFinished(Direction dir) {
        tick();
        if (dir == Direction.FORWARDS) {
            return animation.isDone() && animation.getValue() >= 0.99f;
        } else {
            return animation.isDone() && animation.getValue() <= 0.01f;
        }
    }
}