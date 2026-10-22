package dexum.client.utils.render;

import dexum.client.utils.render.msdf.Msdf;
import dexum.client.utils.render.msdf.MsdfFont;
import dexum.client.utils.render.msdf.MsdfManager;
import dexum.client.utils.render.pipeline.*;
import dexum.client.utils.render.utils.ColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RenderUtil {
    private static final List<Runnable> OVERRIDE_TASKS = new ArrayList<>();
    private static final float Z_OVERRIDE = 0.0f;

    // Thread-local GuiGraphicsExtractor — set at the start of each render frame
    private static final ThreadLocal<GuiGraphicsExtractor> CURRENT_CONTEXT = new ThreadLocal<>();

    public static void setContext(GuiGraphicsExtractor ctx) {
        CURRENT_CONTEXT.set(ctx);
    }

    public static GuiGraphicsExtractor getContext() {
        return CURRENT_CONTEXT.get();
    }

    public static void addOverrideTask(Runnable runnable) {
        OVERRIDE_TASKS.add(runnable);
    }

    public static int getFixedScaledWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    public static int getFixedScaledHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    public static Matrix4f createProjection() {
        return new Matrix4f().ortho(0, (float) getFixedScaledWidth(),
                (float) getFixedScaledHeight(), 0, -1000, 1000);
    }

    public static float getScaleFactor() { return 1.0f; }
    public static float convertX(float x) { return x; }
    public static float convertY(float y) { return y; }

    public static void rect(Matrix4f matrix, float x, float y, float width, float height, float tl, float tr, float br,
                            float bl, boolean overrideContext, Color... colors) {
        int color = colors.length > 0 ? ColorHelper.convertColor(colors[0]) : 0xFFFFFFFF;
        if (overrideContext) {
            OVERRIDE_TASKS.add(() -> RectPipeline.draw(matrix, x, y, width, height, tl, tr, br, bl, Z_OVERRIDE, color));
            return;
        }
        RectPipeline.draw(matrix, x, y, width, height, tl, tr, br, bl, Z_OVERRIDE, color);
    }

    public static void rect(float x, float y, float width, float height, float tl, float tr, float br,
                            float bl, Color... colors) {
        rect(createProjection(), x, y, width, height, tl, tr, br, bl, false, colors);
    }

    public static void rect(float x, float y, float width, float height, float radius, Color... colors) {
        rect(createProjection(), x, y, width, height, radius, radius, radius, radius, false, colors);
    }

    public static void blur(float x, float y, float width, float height, float radius, float strength) {
        BlurPipeline.draw(createProjection(), x, y, width, height, radius, strength, Z_OVERRIDE);
    }

    public static void outline(float x, float y, float width, float height, float tl, float tr, float br, float bl, float thickness, Color... colors) {
        int color = colors.length > 0 ? ColorHelper.convertColor(colors[0]) : 0xFFFFFFFF;
        OutlinePipeline.draw(createProjection(), x, y, width, height, tl, tr, br, bl, thickness, Z_OVERRIDE, color);
    }

    public static void outline(float x, float y, float width, float height, float radius, float thickness, Color... colors) {
        int color = colors.length > 0 ? ColorHelper.convertColor(colors[0]) : 0xFFFFFFFF;
        OutlinePipeline.draw(createProjection(), x, y, width, height, radius, radius, radius, radius, thickness, Z_OVERRIDE, color);
    }

    public static void textGlow(GuiGraphicsExtractor context, float x, float y, String text, float size, Color textColor, Color glowColor, float intensity) {
        Msdf.glowText(context, text, x, y, size, ColorHelper.convertColor(textColor), ColorHelper.convertColor(glowColor), intensity, false);
    }

    public static void textGlow(GuiGraphicsExtractor context, MsdfFont font, float x, float y, String text, float size, Color textColor, Color glowColor, float intensity) {
        Msdf.glowText(context, font, text, x, y, size, ColorHelper.convertColor(textColor), ColorHelper.convertColor(glowColor), intensity, false);
    }

    public static void text(GuiGraphicsExtractor context, float x, float y, String text, float size, Color color) {
        Msdf.text(context, text, x, y, size, ColorHelper.convertColor(color), false);
    }

    public static void text(GuiGraphicsExtractor context, float x, float y, String text, float size, Color color, String align) {
        if (align.equals("center")) {
            Msdf.textCentered(context, text, x, y, size, ColorHelper.convertColor(color));
        } else if (align.equals("right")) {
            Msdf.textRight(context, text, x, y, size, ColorHelper.convertColor(color));
        } else {
            Msdf.text(context, text, x, y, size, ColorHelper.convertColor(color));
        }
    }

    public static void text(GuiGraphicsExtractor context, float x, float y, String text, float size, Color startColor, Color endColor, String align) {
        if (align.equals("center")) {
            Msdf.textCentered(context, text, x, y, size, ColorHelper.convertColor(startColor), ColorHelper.convertColor(endColor));
        } else if (align.equals("right")) {
            Msdf.textRight(context, text, x, y, size, ColorHelper.convertColor(startColor), ColorHelper.convertColor(endColor));
        } else {
            Msdf.textGradient(context, text, x, y, size, ColorHelper.convertColor(startColor), ColorHelper.convertColor(endColor));
        }
    }

    public static void text(GuiGraphicsExtractor context, MsdfFont font, float x, float y, String text, float size, Color color) {
        Msdf.text(context, font, text, x, y, size, ColorHelper.convertColor(color), false);
    }

    public static void text(GuiGraphicsExtractor context, float x, float y, String text, float size, Color... color) {
        Msdf.textColored(context, MsdfManager.MONTSERRAT, text, x, y, size, false, ColorHelper.convertColor(color));
    }

    public static void texture(float x, float y, float size, Identifier texture, float radius, Color color) {
        var client = Minecraft.getInstance();
        var tex = client.getTextureManager().getTexture(texture);
        TexturePipeline.draw(createProjection(), x, y, size, tex.getTextureView(), ColorHelper.convertColor(color), radius, Z_OVERRIDE);
    }

    public static void textureGlow(float x, float y, float size, Identifier texture, float radius, float glowSize, float glowIntensity, Color color) {
        var client = Minecraft.getInstance();
        var tex = client.getTextureManager().getTexture(texture);
        TextureGlowPipeline.draw(createProjection(), x, y, size, tex.getTextureView(), ColorHelper.convertColor(color), radius, glowSize, glowIntensity, Z_OVERRIDE);
    }

    public static void glowCircle(float x, float y, float size, Color color) {
        GlowPipeline.draw(createProjection(), x, y, size, ColorHelper.convertColor(color), Z_OVERRIDE);
    }

    public static void glow(float x, float y, float width, float height, float thickness, float radius, Color color) {
        glow(x, y, width, height, thickness, radius, 0f, 1f, 0f, color);
    }

    public static void glow(float x, float y, float width, float height, float thickness, float radius, float progress, Color color) {
        glow(x, y, width, height, thickness, radius, 0f, 1f, Math.clamp(progress, 0f, 1f), color);
    }

    public static void glow(float x, float y, float width, float height, float thickness, float radius,
                            float glowSize, float glowIntensity, float glowSoftness, Color color) {
        GlowOutlinePipeline.draw(createProjection(), x, y, width, height, thickness,
                ColorHelper.convertColor(color), radius,
                glowSize, Math.clamp(glowIntensity, 0, 1), glowSoftness, 1, Z_OVERRIDE);
    }

    public static void shadow(float x, float y, float width, float height,
                              float radius, float size, float intensity,
                              float softness, Color color) {
        if (size <= 0f || intensity <= 0f) return;
        GlowOutlinePipeline.draw(createProjection(), x, y, width, height,
                0f, ColorHelper.convertColor(color), radius,
                size, Math.clamp(intensity, 0f, 1f), Math.max(0.1f, softness),
                1, Z_OVERRIDE);
    }

    public static void shadow(float x, float y, float width, float height, float radius, Color color) {
        shadow(x, y, width, height, radius, 12f, 0.55f, 2.2f, color);
    }

    public static boolean isOverrideActive() {
        var client = Minecraft.getInstance();
        return client.gui.screen() == null || client.gui.screen() instanceof ChatScreen;
    }
}
