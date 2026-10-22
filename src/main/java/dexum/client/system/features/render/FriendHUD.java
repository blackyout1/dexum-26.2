package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.Render2DEvent;
import dexum.client.system.friends.FriendManager;
import dexum.client.system.settings.impl.BooleanSetting;
import dexum.client.utils.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@NewFunction(name = "FriendHUD", desc = "Панель с HP и пингом онлайн-друзей", category = Category.RENDER)
public class FriendHUD extends Function {

    private final BooleanSetting showHealth = new BooleanSetting("Здоровье", true);
    private final BooleanSetting showPing   = new BooleanSetting("Пинг", true);

    // All sizes in real GUI pixels (MC font: glyph=8px, lineHeight=9px)
    private static final int ROW_H    = 13;
    private static final int HEADER_H = 13;
    private static final int PAD_X    = 6;
    private static final int PAD_Y    = 2;   // (13-9)/2 = 2 → centres text vertically
    private static final int W        = 120;
    private static final int RADIUS   = 3;

    private static final int BG        = rgba(10, 10, 10, 200);
    private static final int HEADER_BG = rgba(15, 40, 15, 220);
    private static final int TITLE_C   = rgba(80, 220, 90, 255);
    private static final int NAME_C    = rgba(210, 210, 210, 255);
    private static final int OFFLINE_C = rgba(100, 100, 100, 180);
    private static final int PING_C    = rgba(120, 130, 120, 200);
    private static final int SEP_C     = rgba(30, 60, 30, 160);

    private float posX = -1f, posY = -1f;
    private boolean dragging = false, wasDown = false;
    private float dragOffX, dragOffY;

    public FriendHUD() { addSettings(showHealth, showPing); }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (nullCheck.all()) return;
        GuiGraphicsExtractor ctx = event.getContext();

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        // Build friend list
        List<FriendEntry> entries = new ArrayList<>();
        if (mc.player.connection != null) {
            for (PlayerInfo info : mc.player.connection.getOnlinePlayers()) {
                String name = info.getProfile().name();
                if (!FriendManager.isFriend(name)) continue;
                int hp = -1;
                for (var entity : mc.level.entitiesForRendering()) {
                    if (entity instanceof Player pe && pe.getGameProfile().name().equals(name)) {
                        hp = Math.round(pe.getHealth());
                        break;
                    }
                }
                entries.add(new FriendEntry(name, hp, info.getLatency()));
            }
        }
        for (String name : FriendManager.getAll()) {
            if (entries.stream().noneMatch(e -> e.name.equals(name)))
                entries.add(new FriendEntry(name, -1, -1));
        }
        if (entries.isEmpty()) return;

        int totalH = HEADER_H + entries.size() * ROW_H + PAD_Y;
        if (posX < 0) { posX = sw - W - 10f; posY = sh / 2f - totalH / 2f; }

        // Drag
        long win = mc.getWindow().handle();
        double[] rx = new double[1], ry = new double[1];
        GLFW.glfwGetCursorPos(win, rx, ry);
        float mxf = (float)(rx[0] * sw / mc.getWindow().getScreenWidth());
        float myf = (float)(ry[0] * sh / mc.getWindow().getScreenHeight());
        boolean lmb = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean over = mxf >= posX && mxf <= posX + W && myf >= posY && myf <= posY + totalH;
        if (lmb && !wasDown && over) { dragging = true; dragOffX = mxf - posX; dragOffY = myf - posY; }
        if (!lmb) dragging = false;
        if (dragging) { posX = mxf - dragOffX; posY = myf - dragOffY; }
        wasDown = lmb;
        posX = Math.max(0, Math.min(sw - W, posX));
        posY = Math.max(0, Math.min(sh - totalH, posY));

        int x = Math.round(posX);
        int y = Math.round(posY);

        // Background + header
        RenderUtil.rect(x, y, W, totalH, RADIUS, new Color(10, 10, 10, 200));
        RenderUtil.rect(x, y, W, HEADER_H, RADIUS, new Color(15, 40, 15, 220));

        // Header title
        String title = "Друзья (" + entries.size() + ")";
        ctx.text(mc.font, title, x + PAD_X, y + PAD_Y, TITLE_C);

        // Separator line
        ctx.fill(x, y + HEADER_H, x + W, y + HEADER_H + 1, SEP_C);

        // Rows
        int rowY = y + HEADER_H + 1;
        for (FriendEntry e : entries) {
            boolean online = e.ping >= 0;
            int nameColor = online ? NAME_C : OFFLINE_C;
            int ty = rowY + PAD_Y;

            ctx.text(mc.font, e.name, x + PAD_X, ty, nameColor);

            // Right-side info
            StringBuilder right = new StringBuilder();
            if (showHealth.isEnabled() && e.hp >= 0) right.append(e.hp).append("hp ");
            if (showPing.isEnabled() && online)       right.append(e.ping).append("ms");
            if (!online) right.append("offline");

            if (!right.isEmpty()) {
                String rs = right.toString().trim();
                int rw = mc.font.width(rs);
                ctx.text(mc.font, rs, x + W - PAD_X - rw, ty, PING_C);
            }

            rowY += ROW_H;
        }
    }

    private record FriendEntry(String name, int hp, int ping) {}

    private static int rgba(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
