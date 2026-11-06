package dexum.client.system.hud;

import dexum.client.utils.render.RenderUtil;
import dexum.client.utils.render.msdf.Msdf;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.Identifier;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Watermark HUD element - displays logo, username, ping, FPS, coordinates, and time.
 * Extends AbstractHudElement for full dragging and animation support.
 */
public class WatermarkHud extends AbstractHudElement {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private float animatedWidth = 200.0f;

    public WatermarkHud() {
        super("Watermark", 10, 10, 200, 20);
        startAnimation();
    }

    @Override
    public void drawElement(GuiGraphicsExtractor context, float alpha) {
        if (alpha <= 0.01f || mc.player == null) return;

        float scale = 1.0f;
        float h = 18.0f * scale;
        float radius = 9.0f * scale;
        float fontSize = 6.5f * scale;
        float iconSize = 10.0f * scale;

        // Collect data
        String username = mc.getUser().getName();
        int ping = getPing();
        String pingText = ping + " ms";
        String fpsText = mc.getFps() + " FPS";
        String coordsText = (int)mc.player.getX() + " " + (int)mc.player.getY() + " " + (int)mc.player.getZ();
        String timeText = LocalTime.now().format(TIME_FMT);

        // Calculate widths
        float wName = Msdf.width(Msdf.BOLD, username, fontSize);
        float wPing = Msdf.width(Msdf.BOLD, pingText, fontSize);
        float wFps = Msdf.width(Msdf.BOLD, fpsText, fontSize);
        float wCoords = Msdf.width(Msdf.BOLD, coordsText, fontSize);
        float wTime = Msdf.width(Msdf.BOLD, timeText, fontSize);

        // Calculate total width
        float cx = 8.0f * scale;
        cx += 20.0f * scale + 6.0f * scale;     // logo
        cx += 12.0f * scale + 4.0f * scale;     // face
        cx += wName + 8.0f * scale;             // name
        cx += iconSize + 3.0f * scale + wPing + 8.0f * scale;   // ping
        cx += iconSize + 3.0f * scale + wFps + 8.0f * scale;    // fps
        cx += iconSize + 3.0f * scale + wCoords + 8.0f * scale; // coords
        cx += iconSize + 3.0f * scale + wTime + 8.0f * scale;   // time

        float targetWidth = cx;
        animatedWidth = lerp(animatedWidth, targetWidth, 0.05f);
        width = animatedWidth;
        height = h;

        float drawX = x;
        float drawY = y;

        // Draw background with glassmorphism effect
        int steps = 8;
        for (int i = 1; i <= steps; i++) {
            float alphaFactor = (float) Math.pow(1f - (i / (float)steps), 1.5f);
            int shadowAlpha = (int)(70 * alphaFactor * alpha);
            float offset = i * 0.75f * scale;
            RenderUtil.outline(drawX - offset, drawY - offset, width + offset*2, h + offset*2,
                    radius + offset, 0.75f * scale, new Color(0, 0, 0, shadowAlpha));
        }
        RenderUtil.glassmorphism(drawX, drawY, width, h, radius, 10.0f * scale,
                new Color(20, 20, 25, (int)(120 * alpha)).getRGB());

        int white = new Color(255, 255, 255, (int)(255 * alpha)).getRGB();
        float textY = drawY + h / 2.0f - (fontSize / 2.0f) - 0.5f;
        float curX = drawX + 8.0f * scale;

        // Draw logo
        RenderUtil.texture(curX, drawY + h / 2.0f - 10.0f * scale, 20.0f * scale,
                Identifier.fromNamespaceAndPath("dexum", "textures/logo.png"), 0.0f, Color.WHITE);
        curX += 20.0f * scale + 6.0f * scale;

        // Draw face
        drawFace(context, curX, drawY + h / 2.0f - 6.0f * scale, 12.0f * scale, alpha);
        curX += 12.0f * scale + 4.0f * scale;

        // Draw name
        drawText(context, username, curX, textY, fontSize, white, alpha);
        curX += wName + 8.0f * scale;

        // Draw ping
        RenderUtil.texture(curX, drawY + h / 2.0f - 5.0f * scale, iconSize,
                Identifier.fromNamespaceAndPath("dexum", "textures/icons/ping.png"), 0.0f, Color.WHITE);
        curX += iconSize + 3.0f * scale;
        drawText(context, pingText, curX, textY, fontSize, white, alpha);
        curX += wPing + 8.0f * scale;

        // Draw FPS
        RenderUtil.texture(curX, drawY + h / 2.0f - 5.0f * scale, iconSize,
                Identifier.fromNamespaceAndPath("dexum", "textures/icons/fps.png"), 0.0f, Color.WHITE);
        curX += iconSize + 3.0f * scale;
        drawText(context, fpsText, curX, textY, fontSize, white, alpha);
        curX += wFps + 8.0f * scale;

        // Draw coordinates
        RenderUtil.texture(curX, drawY + h / 2.0f - 5.0f * scale, iconSize,
                Identifier.fromNamespaceAndPath("dexum", "textures/icons/coordinates.png"), 0.0f, Color.WHITE);
        curX += iconSize + 3.0f * scale;
        drawText(context, coordsText, curX, textY, fontSize, white, alpha);
        curX += wCoords + 8.0f * scale;

        // Draw time
        RenderUtil.texture(curX, drawY + h / 2.0f - 5.0f * scale, iconSize,
                Identifier.fromNamespaceAndPath("dexum", "textures/icons/time.png"), 0.0f, Color.WHITE);
        curX += iconSize + 3.0f * scale;
        drawText(context, timeText, curX, textY, fontSize, white, alpha);
    }

    private void drawText(GuiGraphicsExtractor context, String text, float x, float y,
                          float size, int color, float alpha) {
        // Shadow
        int shadowColor = new Color(0, 0, 0, (int)(100 * alpha)).getRGB();
        Msdf.text(context, Msdf.BOLD, text, x + 0.5f, y + 0.5f, size, shadowColor, false);
        // Main text
        Msdf.text(context, Msdf.BOLD, text, x, y, size, color, false);
    }

    private void drawFace(GuiGraphicsExtractor context, float faceX, float faceY,
                          float faceSize, float alpha) {
        if (mc.getConnection() == null || mc.player == null) return;

        PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        if (info == null) return;

        Identifier skin = info.getSkin().body().texturePath();
        if (skin == null) return;

        // Face: 8,8 - 16,16
        RenderUtil.textureUV(faceX, faceY, faceSize, faceSize, 8, 8, 16, 16, skin);

        // Hat (layer 2): 40,8 - 48,16
        float hatScale = 1.1f;
        float hatSize = faceSize * hatScale;
        float hatOffset = (hatSize - faceSize) / 2.0f;
        RenderUtil.textureUV(faceX - hatOffset, faceY - hatOffset, hatSize, hatSize, 40, 8, 48, 16, skin);
    }

    private int getPing() {
        if (mc.getConnection() != null && mc.player != null) {
            PlayerInfo entry = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            if (entry != null) {
                return entry.getLatency();
            }
        }
        return 0;
    }

    private float lerp(float current, float target, float speed) {
        return current + (target - current) * speed;
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}