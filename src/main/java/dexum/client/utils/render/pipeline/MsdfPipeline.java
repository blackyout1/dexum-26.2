package dexum.client.utils.render.pipeline;

import dexum.client.utils.render.RenderUtil;
import dexum.client.utils.render.msdf.MsdfFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.font.FontManager;
import org.joml.Matrix4f;

import java.lang.reflect.Field;

public class MsdfPipeline {
    private static Font customFont = null;

    public static Font getCustomFont() {
        if (customFont == null) {
            try {
                Field field = Minecraft.class.getDeclaredField("fontManager");
                field.setAccessible(true);
                FontManager manager = (FontManager) field.get(Minecraft.getInstance());

                // В 26.2 метод называется createFont()
                customFont = manager.createFont();
            } catch (Exception e) {
                System.err.println("[Dexum] Не удалось загрузить кастомный шрифт, используем стандартный.");
                customFont = Minecraft.getInstance().font;
            }
        }
        return customFont;
    }

    public static void drawString(Matrix4f matrix, MsdfFont font, String text, float x, float y, float size, int color, float rotation, float z) {
        GuiGraphicsExtractor ctx = RenderUtil.getContext();
        if (ctx == null || text == null || text.isEmpty()) return;

        float scale = size / 11.0f;

        ctx.pose().pushMatrix();
        ctx.pose().translate(x, y);
        ctx.pose().scale(scale, scale);

        ctx.text(getCustomFont(), text, 0, 0, color);

        ctx.pose().popMatrix();
    }

    public static void drawStringColored(Matrix4f matrix, MsdfFont font, String text, float x, float y, float size, int[] colors, float z) {
        int color = (colors != null && colors.length > 0) ? colors[0] : 0xFFFFFFFF;
        drawString(matrix, font, text, x, y, size, color, 0.0f, z);
    }

    public static void init() {}
    public static void shutdown() {}
}