package dexum.client.utils.render.pipeline;

import dexum.client.utils.render.RenderUtil;
import dexum.client.utils.render.msdf.MsdfFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix4f;

/**
 * MsdfPipeline — fallback using vanilla MC font.
 * Renders at native scale (no pose.scale) for correct positioning.
 * The 'size' parameter is ignored in fallback — MC font is always 8px tall.
 */
public class MsdfPipeline {

    public static void drawString(Matrix4f matrix, MsdfFont font, String text,
                                  float x, float y, float size, int color,
                                  float rotation, float z) {
        GuiGraphicsExtractor ctx = RenderUtil.getContext();
        if (ctx == null || text == null || text.isEmpty()) return;
        ctx.text(Minecraft.getInstance().font, text, Math.round(x), Math.round(y), color);
    }

    public static void drawStringColored(Matrix4f matrix, MsdfFont font, String text,
                                         float x, float y, float size, int[] colors, float z) {
        GuiGraphicsExtractor ctx = RenderUtil.getContext();
        if (ctx == null || text == null || text.isEmpty()) return;
        int color = (colors != null && colors.length > 0) ? colors[0] : 0xFFFFFFFF;
        ctx.text(Minecraft.getInstance().font, text, Math.round(x), Math.round(y), color);
    }

    public static void init() {}
    public static void shutdown() {}
}
