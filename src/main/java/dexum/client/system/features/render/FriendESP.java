package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.Render2DEvent;
import dexum.client.system.friends.FriendManager;
import dexum.client.system.settings.impl.BooleanSetting;
import dexum.client.system.settings.impl.NumberSetting;
import dexum.client.utils.render.RenderUtil;
import dexum.client.utils.render.WorldToScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

@NewFunction(name = "FriendESP", desc = "Подсвечивает друзей через стены", category = Category.RENDER)
public class FriendESP extends Function {

    private final BooleanSetting showBox  = new BooleanSetting("Рамка", true);
    private final BooleanSetting showLine = new BooleanSetting("Линия к другу", false);
    private final BooleanSetting showSelf = new BooleanSetting("Себя", false);
    private final NumberSetting angle     = new NumberSetting("Угол обзора", 90.0, 10.0, 180.0, 5.0);

    private static final Color BOX_COLOR  = new Color(60, 220, 80, 200);
    private static final Color BOX_FILL   = new Color(30, 180, 50, 18);
    private static final Color LINE_COLOR = new Color(60, 220, 80, 140);

    public FriendESP() {
        addSettings(showBox, showLine, showSelf, angle);
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (nullCheck.all()) return;

        GuiGraphicsExtractor ctx = event.getContext();
        float td = event.getTickCounter().getGameTimeDeltaPartialTick(false);

        // Кэшируем данные камеры один раз
        double camX = mc.player.getX();
        double camY = mc.player.getEyeY();
        double camZ = mc.player.getZ();
        Vec3 look = mc.player.getLookAngle();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player player)) continue;
            if (player == mc.player && !showSelf.isEnabled()) continue;
            if (!FriendManager.isFriend(player.getGameProfile().name())) continue;
            if (!player.isAlive()) continue;

            // Проверка угла обзора
            if (!shouldRender(player, camX, camY, camZ, look)) continue;

            // Используем WorldToScreen для правильной проекции
            double px = Mth.lerp(td, player.xo, player.getX());
            double py = Mth.lerp(td, player.yo, player.getY()) + player.getEyeHeight();
            double pz = Mth.lerp(td, player.zo, player.getZ());

            float[] screen = WorldToScreen.project(px, py, pz);
            if (screen == null) continue;

            float screenX = screen[0];
            float screenY = screen[1];

            // Размер бокса в пикселях (примерно по расстоянию)
            double distance = Math.sqrt((px - camX) * (px - camX) + (py - camY) * (py - camY) + (pz - camZ) * (pz - camZ));
            float size = (float) (40.0 / distance * 10.0);
            size = Math.max(8f, Math.min(60f, size));

            if (showBox.isEnabled()) {
                int bx = Math.round(screenX - size / 2);
                int by = Math.round(screenY - size / 2);
                int bw = Math.round(size);
                int bh = Math.round(size * 1.8f);
                RenderUtil.rect(bx, by, bw, bh, 0f, BOX_FILL);
                RenderUtil.outline(bx, by, bw, bh, 0f, 1.2f, BOX_COLOR);
            }
        }

        if (showLine.isEnabled()) {
            drawLines(camX, camY, camZ);
        }
    }

    private boolean shouldRender(Player player, double camX, double camY, double camZ, Vec3 look) {
        Vec3 eye = new Vec3(camX, camY, camZ);
        Vec3 to = player.getEyePosition().subtract(eye);
        double dist = to.length();
        if (dist < 0.1) return true;
        to = to.normalize();
        double dot = look.dot(to);
        double angleRad = Math.acos(Math.min(1.0, Math.max(-1.0, dot)));
        return Math.toDegrees(angleRad) <= angle.getValue();
    }

    private void drawLines(double camX, double camY, double camZ) {
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player player)) continue;
            if (player == mc.player && !showSelf.isEnabled()) continue;
            if (!FriendManager.isFriend(player.getGameProfile().name())) continue;
            if (!player.isAlive()) continue;
            if (!shouldRender(player, camX, camY, camZ, mc.player.getLookAngle())) continue;

            // Используем WorldToScreen для правильной проекции
            double px = player.getX();
            double py = player.getEyeY();
            double pz = player.getZ();

            float[] screen = WorldToScreen.project(px, py, pz);
            if (screen == null) continue;

            float tx = screen[0];
            float ty = screen[1];

            int sw = mc.getWindow().getGuiScaledWidth();
            int sh = mc.getWindow().getGuiScaledHeight();
            float x1 = sw / 2f, y1 = sh / 2f;
            float dx2 = tx - x1, dy2 = ty - y1;
            float len = (float) Math.sqrt(dx2 * dx2 + dy2 * dy2);
            if (len < 1f) continue;

            float nx = -dy2 / len, ny = dx2 / len;
            float thickness = 0.6f;
            float steps = Math.max(1f, len / 4f);
            for (float i = 0; i <= steps; i++) {
                float t = i / steps;
                float px2 = x1 + dx2 * t, py2 = y1 + dy2 * t;
                RenderUtil.rect(px2 + nx * thickness, py2 + ny * thickness,
                        2 * thickness, 2 * thickness, 0f, LINE_COLOR);
            }
        }
    }
}
