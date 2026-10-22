package dexum.client.utils.render.pipeline;

import dexum.client.utils.render.RenderUtil;
import dexum.client.utils.render.msdf.MsdfFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix4f;

/** MsdfGlowPipeline — fallback without glow effect. */
public class MsdfGlowPipeline {

    public static void drawString(Matrix4f matrix, MsdfFont font, String text,
                                  float x, float y, float size,
                                  int textColor, int glowColor, float glowIntensity, float z) {
        GuiGraphicsExtractor ctx = RenderUtil.getContext();
        if (ctx == null || text == null || text.isEmpty()) return;
        ctx.text(Minecraft.getInstance().font, text, Math.round(x), Math.round(y), textColor);
    }

    public static void init() {}
    public static void shutdown() {}
}
