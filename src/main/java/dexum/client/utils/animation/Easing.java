package dexum.client.utils.animation;

/**
 * Портировано из wtf.dexum.base.animations.base.Easing.
 */
@FunctionalInterface
public interface Easing {

    float ease(float t);

    // ── Линейная ─────────────────────────────────────────────────────────────
    Easing LINEAR = t -> t;

    // ── Квадратичные ─────────────────────────────────────────────────────────
    Easing QUAD_IN    = t -> t * t;
    Easing QUAD_OUT   = t -> t * (2 - t);
    Easing QUAD_IN_OUT = t -> t < 0.5f ? 2*t*t : -1 + (4-2*t)*t;

    // ── Кубические ───────────────────────────────────────────────────────────
    Easing CUBIC_IN    = t -> t * t * t;
    Easing CUBIC_OUT   = t -> { float f = t - 1; return f*f*f + 1; };
    Easing CUBIC_IN_OUT = t -> t < 0.5f ? 4*t*t*t : (t-1)*(2*t-2)*(2*t-2)+1;

    // ── Quartic ──────────────────────────────────────────────────────────────
    Easing QUARTIC_IN   = t -> t * t * t * t;
    Easing QUARTIC_OUT  = t -> { float f = t - 1; return 1 - f*f*f*f; };

    // ── Quintic ──────────────────────────────────────────────────────────────
    Easing QUINTIC_IN   = t -> t * t * t * t * t;
    Easing QUINTIC_OUT  = t -> { float f = t - 1; return f*f*f*f*f + 1; };

    // ── Круговые ─────────────────────────────────────────────────────────────
    Easing CIRC_IN  = t -> 1 - (float) Math.sqrt(1 - t*t);
    Easing CIRC_OUT = t -> (float) Math.sqrt((2 - t) * t);

    // ── Синусоидальные ────────────────────────────────────────────────────────
    Easing SINE_IN  = t -> 1 - (float) Math.cos(t * Math.PI / 2);
    Easing SINE_OUT = t -> (float) Math.sin(t * Math.PI / 2);

    // ── Экспоненциальные ─────────────────────────────────────────────────────
    Easing EXPO_IN  = t -> t == 0 ? 0 : (float) Math.pow(2, 10*(t-1));
    Easing EXPO_OUT = t -> t == 1 ? 1 : 1 - (float) Math.pow(2, -10*t);

    // ── Плавное step ─────────────────────────────────────────────────────────
    Easing SMOOTH_STEP = t -> t * t * (3 - 2 * t);

    // ── Back (с откатом) ─────────────────────────────────────────────────────
    Easing BACK_OUT = t -> {
        float c = 1.70158f;
        float f = t - 1;
        return f * f * ((c + 1) * f + c) + 1;
    };
    Easing BACK_IN = t -> {
        float c = 1.70158f;
        return t * t * ((c + 1) * t - c);
    };

    // ── Elastic ──────────────────────────────────────────────────────────────
    Easing ELASTIC_OUT = t -> {
        if (t == 0 || t == 1) return t;
        float p = 0.3f;
        return (float)(Math.pow(2, -10*t) * Math.sin((t - p/4) * (2*Math.PI)/p) + 1);
    };
}
