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
    private static final Color LINE_COLOR = new Color(60, 220, 80, 180);

    public FriendESP() {
        addSettings(showBox, showLine, showSelf, angle);
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (nullCheck.all()) return;

        GuiGraphicsExtractor ctx = event.getContext();
        float td = event.getTickCounter().getGameTimeDeltaPartialTick(false);

        double camX = mc.player.getX();
        double camY = mc.player.getEyeY();
        double camZ = mc.player.getZ();
        Vec3 look = mc.player.getLookAngle();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player player)) continue;
            if (player == mc.player && !showSelf.isEnabled()) continue;
            if (!FriendManager.isFriend(player.getGameProfile().name())) continue;
            if (!player.isAlive()) continue;
            if (!shouldRender(player, camX, camY, camZ, look)) continue;

            double py = Mth.lerp(td, player.yo, player.getY()) + (player == mc.player ? 1.0 : player.getEyeHeight());
            double px = Mth.lerp(td, player.xo, player.getX());
            double pz = Mth.lerp(td, player.zo, player.getZ());

            float[] screen = WorldToScreen.project(px, py, pz);
            if (screen == null) continue;

            float screenX = screen[0];
            float screenY = screen[1];

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
            drawLines(ctx, td, camX, camY, camZ);
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

    private void drawLines(GuiGraphicsExtractor ctx, float td, double camX, double camY, double camZ) {
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        float cx = sw / 2f, cy = sh / 2f;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player player)) continue;
            if (player == mc.player) continue;
            if (!FriendManager.isFriend(player.getGameProfile().name())) continue;
            if (!player.isAlive()) continue;

            // Если друг вне поля зрения (за спиной), мы всё равно рисуем линию к краю экрана
            // для этого используем математику углов
            Vec3 eye = mc.player.getEyePosition();
            Vec3 target = player.getBoundingBox().getCenter();
            Vec3 dir = target.subtract(eye);
            boolean inFront = dir.dot(mc.player.getLookAngle()) > 0;

            float[] screen = WorldToScreen.project(Mth.lerp(td, player.xo, player.getX()),
                    Mth.lerp(td, player.yo, player.getY()) + player.getEyeHeight(),
                    Mth.lerp(td, player.zo, player.getZ()));

            float tx, ty;

            if (screen == null || !inFront) {
                // Друг за спиной или за экраном — вычисляем угол и ведем линию к краю
                double yawTo = Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90.0;
                double deltaYaw = Mth.wrapDegrees(yawTo - mc.player.getYRot());
                float rad = (float) Math.toRadians(deltaYaw);

                // Линия от центра в сторону угла
                tx = cx + (float) Math.sin(rad) * 10000f;
                ty = cy - (float) Math.cos(rad) * 10000f;
            } else {
                tx = screen[0];
                ty = screen[1];
            }

            // Обрезаем линию до границ экрана (Liang-Barsky)
            float[] clipped = clipLine(cx, cy, tx, ty, 0, 0, sw, sh);
            if (clipped == null) continue;

            // Рисуем гладкую линию с шагом 1 пиксель
            drawLineSmooth(ctx, cx, cy, clipped[2], clipped[3], LINE_COLOR);
        }
    }

    // Обрезка линии по границам экрана
    private float[] clipLine(float x1, float y1, float x2, float y2, float minX, float minY, float maxX, float maxY) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float[] p = {-dx, dx, -dy, dy};
        float[] q = {x1 - minX, maxX - x1, y1 - minY, maxY - y1};

        float u1 = 0, u2 = 1;
        for (int i = 0; i < 4; i++) {
            if (p[i] == 0) {
                if (q[i] < 0) return null;
            } else {
                float t = q[i] / p[i];
                if (p[i] < 0) {
                    if (t > u2) return null;
                    if (t > u1) u1 = t;
                } else {
                    if (t < u1) return null;
                    if (t < u2) u2 = t;
                }
            }
        }
        return new float[]{x1, y1, x1 + u2 * dx, y1 + u2 * dy};
    }

    // Плавная отрисовка линии без фризов
    private void drawLineSmooth(GuiGraphicsExtractor ctx, float x1, float y1, float x2, float y2, Color color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        int steps = (int) Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) return;

        float xStep = dx / steps;
        float yStep = dy / steps;

        float x = x1;
        float y = y1;
        int argb = color.getRGB();

        for (int i = 0; i <= steps; i++) {
            int ix = Math.round(x);
            int iy = Math.round(y);
            // Рисуем 2x2 пикселя для толщины, это быстрее чем RenderUtil.rect
            ctx.fill(ix, iy, ix + 2, iy + 2, argb);
            x += xStep;
            y += yStep;
        }
    }
}