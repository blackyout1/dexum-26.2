package dexum.client.utils.render.pipeline;

import dexum.client.utils.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix4f;

/**
 * GlowOutlinePipeline — approximates a glow/shadow by drawing concentric
 * rounded rectangles with decreasing alpha. O(steps * r) total cost.
 */
public class GlowOutlinePipeline {

    public static void draw(Matrix4f matrix, float fx, float fy, float fw, float fh,
                            float thickness, int color, float radius,
                            float glowSize, float glowIntensity, float glowSoftness,
                            int passes, float z) {
        GuiGraphicsExtractor ctx = RenderUtil.getContext();
        if (ctx == null) return;

        int steps = Math.max(1, Math.min(6, Math.round(glowSize))); // cap at 6 for perf
        int baseAlpha = (int)(((color >> 24) & 0xFF) * Math.min(1f, glowIntensity));
        int rgb = color & 0x00FFFFFF;

        for (int i = steps; i >= 1; i--) {
            float t = (float) i / steps;
            int alpha = (int)(baseAlpha * (1f - t * t));
            alpha = Math.max(0, Math.min(255, alpha));
            if (alpha < 3) continue;

            int c = (alpha << 24) | rgb;
            int x = Math.round(fx) - i;
            int y = Math.round(fy) - i;
            int w = Math.round(fw) + i * 2;
            int h = Math.round(fh) + i * 2;
            int r = Math.round(radius) + i;

            RectPipeline.fillRoundedRect(ctx, x, y, w, h, r, c);
        }
    }

    public static void init() {}
    public static void shutdown() {}
}
