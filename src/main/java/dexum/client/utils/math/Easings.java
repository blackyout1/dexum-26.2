package dexum.client.utils.math;

public abstract class Easings {
    public static float InQuad(float t) {
        return t * t;
    }

    public static float OutQuad(float t) {
        return t * (2 - t);
    }

    public static float InOutQuad(float t) {
        return t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    public static float InCubic(float t) {
        return t * t * t;
    }

    public static float OutCubic(float t) {
        float f = t - 1;
        return f * f * f + 1;
    }

    public static float InOutCubic(float t) {
        return t < 0.5f ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
    }

    public static float OutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float)Math.pow(t - 1, 3) + c1 * (float)Math.pow(t - 1, 2);
    }
}
