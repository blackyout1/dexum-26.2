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
import dexum.client.utils.render.msdf.Msdf;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@NewFunction(name = "FriendArrow", desc = "Стрелки вокруг центра экрана в сторону друзей", category = Category.RENDER)
public class FriendArrow extends Function {

    private static final Identifier ARROW_TEXTURE =
            Identifier.fromNamespaceAndPath("dexum", "textures/icons/arrow.png");

    private final BooleanSetting showDistance = new BooleanSetting("Дистанция", true);
    private final NumberSetting  radius       = new NumberSetting("Радиус", 80, 30, 200, 5);
    private final NumberSetting  arrowSize    = new NumberSetting("Размер", 10, 4, 24, 1);

    private final Map<Integer, Float> rotationMap = new HashMap<>();
    private static final float TEXT_SIZE = 5.0f;

    public FriendArrow() {
        addSettings(showDistance, radius, arrowSize);
    }

    @Override
    public void onDisable() {
        rotationMap.clear();
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (nullCheck.all()) return;

        GuiGraphicsExtractor ctx = event.getContext();
        float tickDelta = event.getTickCounter().getGameTimeDeltaPartialTick(false);
        float centerX   = mc.getWindow().getGuiScaledWidth()  / 2f;
        float centerY   = mc.getWindow().getGuiScaledHeight() / 2f;
        float r         = (float) radius.getValue();
        float s         = (float) arrowSize.getValue();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof Player player)) continue;
            if (!player.isAlive()) continue;
            if (!FriendManager.isFriend(player.getGameProfile().name())) continue;

            double diffX = entity.getX() - mc.player.getX();
            double diffZ = entity.getZ() - mc.player.getZ();
            float targetAngle = (float)(Math.toDegrees(Math.atan2(diffZ, diffX))
                    - mc.player.getYRot() - 90.0);

            int id = entity.getId();
            float currentAngle = rotationMap.getOrDefault(id, targetAngle);
            float delta = Mth.wrapDegrees(targetAngle - currentAngle);
            currentAngle += delta * 0.15f * (tickDelta + 1.0f);
            rotationMap.put(id, currentAngle);

            double dist = mc.player.distanceTo(entity);
            float alpha = (float) Mth.clamp(1.0 - (dist / 80.0), 0.25, 1.0);
            int alphaInt = (int)(255 * alpha);
            Color arrowColor = new Color(60, 220, 90, alphaInt);

            float rad = (float) Math.toRadians(currentAngle);
            float ax = centerX + (float) Math.sin(rad) * r;
            float ay = centerY - (float) Math.cos(rad) * r;

            drawRotatedTexture(ax, ay, s, currentAngle, arrowColor);

            if (showDistance.isEnabled()) {
                String distStr = (int) dist + "м";
                float tw = Msdf.width(distStr, TEXT_SIZE);
                float th = Msdf.height(TEXT_SIZE);
                float textX = ax - tw / 2f + (float) Math.sin(rad) * (s * 0.8f + 4f);
                float textY = ay - th / 2f - (float) Math.cos(rad) * (s * 0.8f + 4f);
                RenderUtil.rect(textX - 2f, textY - 1f, tw + 4f, th + 2f, 2f,
                        new Color(10, 10, 10, (int)(150 * alpha)));
                RenderUtil.text(ctx, textX, textY, distStr, TEXT_SIZE,
                        new Color(180, 230, 180, alphaInt));
            }
        }

        if (mc.player.tickCount % 100 == 0) {
            rotationMap.keySet().removeIf(eid -> mc.level.getEntity(eid) == null);
        }
    }

    private void drawRotatedTexture(float cx, float cy, float size, float angleDegrees, Color color) {
        float half = size / 2f;
        Matrix4f proj = RenderUtil.createProjection();
        Matrix4f m = new Matrix4f(proj)
                .translate(cx, cy, 0)
                .rotateZ((float) Math.toRadians(angleDegrees))
                .translate(-half, -half, 0);

        var tex = mc.getTextureManager().getTexture(ARROW_TEXTURE);
        if (tex == null) return;

        dexum.client.utils.render.pipeline.TexturePipeline.draw(
                m, 0, 0, size, tex.getTextureView(),
                color.getRGB(), 0f, 0f
        );
    }
}
