package dexum.client.utils.render.pipeline;

import dexum.client.utils.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix4f;

/**
 * OutlinePipeline — draws a rounded border using 4 edge strips + per-row corner arcs.
 */
public class OutlinePipeline {

    public static void draw(Matrix4f matrix, float fx, float fy, float fw, float fh,
                            float tl, float tr, float br, float bl,
                            float thickness, float z, int color) {
        GuiGraphicsExtractor ctx = RenderUtil.getContext();
        if (ctx == null) return;

        int x = Math.round(fx);
        int y = Math.round(fy);
        int w = Math.round(fw);
        int h = Math.round(fh);
        int r = Math.max(0, Math.min(Math.round((tl + tr + br + bl) / 4f), Math.min(w, h) / 2));
        int t = Math.max(1, Math.round(thickness));

        if (r == 0) {
            // Simple box — 4 strips
            ctx.fill(x, y, x + w, y + t, color);
            ctx.fill(x, y + h - t, x + w, y + h, color);
            ctx.fill(x, y + t, x + t, y + h - t, color);
            ctx.fill(x + w - t, y + t, x + w, y + h - t, color);
            return;
        }

        // Top and bottom straight segments
        ctx.fill(x + r, y, x + w - r, y + t, color);
        ctx.fill(x + r, y + h - t, x + w - r, y + h, color);
        // Left and right straight segments
        ctx.fill(x, y + r, x + t, y + h - r, color);
        ctx.fill(x + w - t, y + r, x + w, y + h - r, color);

        // Corner arcs — per-row, O(r)
        int[] outerInset = new int[r];
        int[] innerInset = new int[r];
        for (int i = 0; i < r; i++) {
            double dist = r - 1 - i;
            outerInset[i] = r - (int) Math.floor(Math.sqrt(r * r - dist * dist));
            int ri = Math.max(0, r - t);
            innerInset[i] = ri > 0
                    ? ri - (int) Math.floor(Math.sqrt((double)ri * ri - dist * dist))
                    : r; // full thickness when ri == 0
        }

        for (int i = 0; i < r; i++) {
            int o = outerInset[i];
            int in = innerInset[i];
            int rowY;

            // Top-left and top-right
            rowY = y + i;
            ctx.fill(x + o, rowY, x + in, rowY + 1, color);           // top-left arc
            ctx.fill(x + w - in, rowY, x + w - o, rowY + 1, color);   // top-right arc

            // Bottom-left and bottom-right
            rowY = y + h - 1 - i;
            ctx.fill(x + o, rowY, x + in, rowY + 1, color);           // bottom-left arc
            ctx.fill(x + w - in, rowY, x + w - o, rowY + 1, color);   // bottom-right arc
        }
    }

    public static void init() {}
    public static void shutdown() {}
}
