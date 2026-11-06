package dexum.client.utils.render;

import dexum.client.utils.render.msdf.Msdf;
import dexum.client.utils.render.msdf.MsdfFont;
import dexum.client.utils.render.pipeline.*;
import dexum.client.utils.render.utils.ColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines; // ИСПРАВЛЕНО ТУТ
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RenderUtil {
    private static final List<Runnable> OVERRIDE_TASKS = new ArrayList<>();
    private static final float Z_OVERRIDE = 0.0f;
    private static final ThreadLocal<GuiGraphicsExtractor> CURRENT_CONTEXT = new ThreadLocal<>();

    public static void setContext(GuiGraphicsExtractor ctx) { CURRENT_CONTEXT.set(ctx); }
    public static GuiGraphicsExtractor getContext() { return CURRENT_CONTEXT.get(); }
    public static void addOverrideTask(Runnable task) { OVERRIDE_TASKS.add(task); }

    public static int getFixedScaledWidth() { return Minecraft.getInstance().getWindow().getGuiScaledWidth(); }
    public static int getFixedScaledHeight() { return Minecraft.getInstance().getWindow().getGuiScaledHeight(); }

    public static Matrix4f createProjection() {
        return new Matrix4f().ortho(0, (float) getFixedScaledWidth(), (float) getFixedScaledHeight(), 0, -1000, 1000);
    }

    public static boolean isOverrideActive() {
        var client = Minecraft.getInstance();
        return client.gui.screen() == null || client.gui.screen() instanceof net.minecraft.client.gui.screens.ChatScreen;
    }

    public static void rect(Matrix4f matrix, float x, float y, float width, float height, float tl, float tr, float br,
                            float bl, boolean overrideContext, Color... colors) {
        int color = colors.length > 0 ? ColorHelper.convertColor(colors[0]) : 0xFFFFFFFF;
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

    public static void outline(float x, float y, float width, float height, float radius, float thickness, Color color) {
        OutlinePipeline.draw(createProjection(), x, y, width, height, radius, radius, radius, radius, thickness, Z_OVERRIDE, color.getRGB());
    }

    public static void textureGlow(float x, float y, float size, Identifier texture, float radius, float glowSize, float glowIntensity, Color color) {
        var client = Minecraft.getInstance();
        var tex = client.getTextureManager().getTexture(texture);
        TextureGlowPipeline.draw(createProjection(), x, y, size, tex.getTextureView(), ColorHelper.convertColor(color), radius, glowSize, glowIntensity, Z_OVERRIDE);
    }

    public static void shadow(float x, float y, float width, float height, float radius, Color color) {
        GlowOutlinePipeline.draw(createProjection(), x, y, width, height, 0f, ColorHelper.convertColor(color), radius, 12f, 0.55f, 2.2f, 1, Z_OVERRIDE);
    }

    public static void glow(float x, float y, float width, float height, float thickness, float radius, float glowSize, float glowIntensity, float glowSoftness, Color color) {
        GlowOutlinePipeline.draw(createProjection(), x, y, width, height, thickness, ColorHelper.convertColor(color), radius, glowSize, glowIntensity, glowSoftness, 1, Z_OVERRIDE);
    }

    public static void glassmorphism(float x, float y, float width, float height, float radius, float blurStrength, int baseColor) {
        GuiGraphicsExtractor ctx = getContext();
        if (ctx == null) return;

        int steps = 8;
        for (int i = 1; i <= steps; i++) {
            float alphaFactor = (float) Math.pow(1f - (i / (float)steps), 1.5);
            int shadowAlpha = (int)(70 * alphaFactor);
            float offset = i * 0.75f;
            outline(x - offset, y - offset, width + offset*2, height + offset*2, radius + offset, 0.75f, new Color(0, 0, 0, shadowAlpha));
        }

        int bgAlpha = (baseColor >> 24) & 0xFF;
        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8) & 0xFF;
        int b = baseColor & 0xFF;

        rect(x, y, width, height, radius, new Color(r, g, b, bgAlpha));
        rect(x, y, width, height / 2, radius, new Color(255, 255, 255, 15));
        outline(x, y, width, height, radius, 0.5f, new Color(255, 255, 255, 40));
    }

    public static void text(GuiGraphicsExtractor context, float x, float y, String text, float size, Color color) {
        Msdf.text(context, text, x, y, size, ColorHelper.convertColor(color), false);
    }

    public static void texture(float x, float y, float size, Identifier texture, float radius, Color color) {
        var tex = Minecraft.getInstance().getTextureManager().getTexture(texture);
        TexturePipeline.draw(createProjection(), x, y, size, tex.getTextureView(), ColorHelper.convertColor(color), radius, Z_OVERRIDE);
    }

    public static void textureUV(float x, float y, float width, float height, float u0, float v0, float u1, float v1, Identifier texture) {
        GuiGraphicsExtractor ctx = getContext();
        if (ctx == null) return;

        ctx.blit(RenderPipelines.GUI_TEXTURED, texture, (int)x, (int)y, (int)width, (int)height, (int)u0, (int)v0, (int)(u1 - u0), (int)(v1 - v0), 64, 64);
    }
}