package dexum.client.utils.animation;

/**
 * Портировано из wtf.dexum.base.animations.base.Animation.
 * Время-based анимация с поддержкой переключения функции сглаживания.
 */
public class Animation {

    private long     duration;
    private Easing   easing;

    private float value;
    private float startValue;
    private float targetValue;
    private long  startTime;
    private boolean done;

    public Animation(long duration, Easing easing) {
        this(duration, 0f, easing);
    }

    public Animation(long duration, float startValue, Easing easing) {
        this.duration   = duration;
        this.easing     = easing;
        this.value      = startValue;
        this.startValue = startValue;
        this.targetValue = startValue;
        this.startTime  = System.currentTimeMillis();
        this.done       = true;
    }

    /**
     * Обновляет целевое значение и при необходимости перезапускает анимацию.
     * Вызывать каждый кадр.
     */
    public void update(float newTarget) {
        if (newTarget != targetValue) {
            startValue   = value;
            targetValue  = newTarget;
            startTime    = System.currentTimeMillis();
            done         = false;
        }
        if (done) return;

        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= duration) {
            value = targetValue;
            done  = true;
            return;
        }

        float t = (float) elapsed / duration;
        value = startValue + (targetValue - startValue) * easing.ease(t);
    }

    /** Мгновенно установить значение без анимации */
    public void setValue(float v) {
        value       = v;
        startValue  = v;
        targetValue = v;
        done        = true;
    }

    public float   getValue()   { return value; }
    public boolean isDone()     { return done; }

    public void setDuration(long d) { this.duration = d; }
    public void setEasing(Easing e) { this.easing   = e; }
}
