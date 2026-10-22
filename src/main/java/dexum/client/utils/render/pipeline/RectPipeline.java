package dexum.client.utils.render.pipeline;

import dexum.client.utils.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix4f;

/**
 * RectPipeline — fast fill via GuiGraphicsExtractor.fill().
 * No per-pixel corner loops — uses a simple 3-strip approach for rounded corners
 * that is O(1) instead of O(r²), keeping frame rate high.
 * True smooth rounding requires a shader; this is the best CPU-side approximation.
 */
public class RectPipeline {

    public static void draw(Matrix4f matrix, float fx, float fy, float fw, float fh,
                            float tl, float tr, float br, float bl, float z, int color) {
        GuiGraphicsExtractor ctx = RenderUtil.getContext();
        if (ctx == null) return;

        int x = Math.round(fx);
        int y = Math.round(fy);
        int w = Math.round(fw);
        int h = Math.round(fh);
        int r = Math.round((tl + tr + br + bl) / 4f);

        fillRoundedRect(ctx, x, y, w, h, r, color);
    }

    /**
     * Draws a rounded rectangle using only fill() calls — O(r) not O(r²).
     * Each corner is approximated with a few horizontal strips, which looks
     * clean at GUI scale ≥ 2 and costs almost nothing.
     */
    public static void fillRoundedRect(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int color) {
        if (w <= 0 || h <= 0) return;
        int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        if (r == 0) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }

        // Precomputed per-row x-inset for a circle of radius r
        // inset[i] = how many pixels to cut from each side at row i from the corner
        // Using the circle equation: inset = r - floor(sqrt(r²-(r-i-1)²))
        int[] inset = new int[r];
        for (int i = 0; i < r; i++) {
            double dist = r - 1 - i;
            inset[i] = r - (int) Math.floor(Math.sqrt(r * r - dist * dist));
        }

        // Top rows (corners cut)
        for (int i = 0; i < r; i++) {
            int cut = inset[i];
            g.fill(x + cut, y + i, x + w - cut, y + i + 1, color);
        }
        // Middle — full width
        g.fill(x, y + r, x + w, y + h - r, color);
        // Bottom rows (corners cut)
        for (int i = 0; i < r; i++) {
            int cut = inset[r - 1 - i];
            g.fill(x + cut, y + h - r + i, x + w - cut, y + h - r + i + 1, color);
        }
    }

    public static void init() {}
    public static void shutdown() {}
}
