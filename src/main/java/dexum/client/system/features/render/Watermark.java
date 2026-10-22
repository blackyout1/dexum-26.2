package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.FunctionManager;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventBus;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.Render2DEvent;
import dexum.client.system.features.player.StreamerMode;
import dexum.client.utils.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@NewFunction(name = "Watermark", desc = "HUD ватермарка", category = Category.RENDER)
public class Watermark extends Function {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // Layout constants — all in GUI pixels (MC font is 8px glyphs, lineHeight=9)
    private static final int   H       = 14;   // bar height
    private static final int   PAD_X   = 6;    // horizontal padding
    private static final int   PAD_Y   = 3;    // vertical padding — centres 8px glyph in 14px bar
    private static final int   SEP_W   = 1;    // separator width
    private static final int   SEP_GAP = 5;    // gap around separator
    private static final int   RADIUS  = 3;

    // Colours
    private static final int BG      = color(13, 13, 13, 215);
    private static final int NAME_C1 = color(110, 110, 255, 255);
    private static final int WHITE   = color(230, 230, 230, 255);
    private static final int DIM     = color(130, 130, 145, 255);
    private static final int SEP_C   = color(55, 55, 65, 180);
    private static final int DRAG_C  = color(255, 255, 255, 18);

    private float posX = -1, posY = -1;
    private boolean dragging = false, wasDown = false;
    private float dragOffX, dragOffY;

    @Override public void onEnable()  { EventBus.register(this); }
    @Override public void onDisable() { EventBus.unregister(this); }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        GuiGraphicsExtractor ctx = event.getContext();

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        StreamerMode streamer = StreamerMode.getInstance();
        String nameStr = (streamer != null && streamer.shouldHideClientName()) ? "Client" : "Dexum";
        String realUser = mc.getUser().getName();
        String userStr  = (streamer != null) ? streamer.getDisplayName(realUser) : realUser;
        String fpsStr   = mc.getFps() + " fps";
        String timeStr  = LocalTime.now().format(TIME_FMT);

        // Calculate total width using real MC font measurements
        int wName = mc.font.width(nameStr);
        int wUser = mc.font.width(userStr);
        int wFps  = mc.font.width(fpsStr);
        int wTime = mc.font.width(timeStr);

        int totalW = PAD_X + wName
                + SEP_GAP + SEP_W + SEP_GAP + wUser
                + SEP_GAP + SEP_W + SEP_GAP + wFps
                + SEP_GAP + SEP_W + SEP_GAP + wTime
                + PAD_X;

        if (posX < 0) {
            posX = (sw - totalW) / 2f;
            posY = sh - H - 8f;
        }

        // Drag handling
        long win = mc.getWindow().handle();
        double[] rx = new double[1], ry = new double[1];
        GLFW.glfwGetCursorPos(win, rx, ry);
        float mx = (float)(rx[0] * sw / mc.getWindow().getScreenWidth());
        float my = (float)(ry[0] * sh / mc.getWindow().getScreenHeight());

        boolean lmb = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean over = mx >= posX && mx <= posX + totalW && my >= posY && my <= posY + H;
        if (lmb && !wasDown && over) { dragging = true; dragOffX = mx - posX; dragOffY = my - posY; }
        if (!lmb) dragging = false;
        if (dragging) { posX = mx - dragOffX; posY = my - dragOffY; }
        wasDown = lmb;

        posX = Math.max(0, Math.min(sw - totalW, posX));
        posY = Math.max(0, Math.min(sh - H, posY));

        int x = Math.round(posX);
        int y = Math.round(posY);

        // Draw background
        if (dragging) RenderUtil.rect(x - 1, y - 1, totalW + 2, H + 2, RADIUS + 1, new Color(255, 255, 255, 18));
        RenderUtil.rect(x, y, totalW, H, RADIUS, new Color(13, 13, 13, 215));

        // Text Y — centred vertically: (H - fontHeight) / 2 = (14 - 9) / 2 = 2 → y+2 (but shadow adds 1)
        int ty = y + PAD_Y;
        int cx = x + PAD_X;

        // "Dexum" — blue accent
        ctx.text(mc.font, nameStr, cx, ty, NAME_C1);
        cx += wName + SEP_GAP;

        // Separator
        ctx.fill(cx, y + 3, cx + SEP_W, y + H - 3, SEP_C);
        cx += SEP_W + SEP_GAP;

        ctx.text(mc.font, userStr, cx, ty, WHITE);
        cx += wUser + SEP_GAP;

        ctx.fill(cx, y + 3, cx + SEP_W, y + H - 3, SEP_C);
        cx += SEP_W + SEP_GAP;

        ctx.text(mc.font, fpsStr, cx, ty, DIM);
        cx += wFps + SEP_GAP;

        ctx.fill(cx, y + 3, cx + SEP_W, y + H - 3, SEP_C);
        cx += SEP_W + SEP_GAP;

        ctx.text(mc.font, timeStr, cx, ty, DIM);
    }

    public float getPosX() { return posX; }
    public float getPosY() { return posY; }
    public void setPosX(float v) { posX = v; }
    public void setPosY(float v) { posY = v; }

    private static int color(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
