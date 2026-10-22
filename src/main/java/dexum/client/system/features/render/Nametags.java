package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.Render2DEvent;
import dexum.client.system.friends.FriendManager;
import dexum.client.system.settings.impl.BooleanSetting;
import dexum.client.system.settings.impl.ColorSetting;
import dexum.client.utils.render.RenderUtil;
import dexum.client.utils.render.WorldToScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@NewFunction(name = "Nametags", desc = "Показывает имена игроков над головой", category = Category.RENDER)
public class Nametags extends Function {

    private final BooleanSetting showArmor = new BooleanSetting("Броня", true);
    private final BooleanSetting showHead  = new BooleanSetting("Голова", true);
    private final BooleanSetting showHealth = new BooleanSetting("Здоровье", true);
    private final BooleanSetting showPing   = new BooleanSetting("Пинг", true);
    private final BooleanSetting showSelf   = new BooleanSetting("Себя", false);

    private final ColorSetting nameColor   = new ColorSetting("Цвет имени", new Color(255, 255, 255, 255));
    private final ColorSetting hpColor     = new ColorSetting("Цвет HP", new Color(255, 255, 255, 255));
    private final ColorSetting bgColor     = new ColorSetting("Цвет фона", new Color(0, 0, 0, 128));
    private final ColorSetting friendTagColor = new ColorSetting("Цвет тега F", new Color(0, 255, 0, 255));

    private static final int SEP = 4;
    private static final int PAD_X = 4;
    private static final int PAD_Y = 2;
    private static final float BG_RADIUS = 2f;

    public Nametags() {
        addSettings(showArmor, showHead, showHealth, showPing, showSelf,
                nameColor, hpColor, bgColor, friendTagColor);
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        GuiGraphicsExtractor ctx = event.getContext();
        if (nullCheck.all()) return;

        Set<UUID> rendered = new HashSet<>();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player player)) continue;
            if (player == mc.player && !showSelf.isEnabled()) continue;
            if (player == mc.player && mc.options.getCameraType().isFirstPerson()) continue;
            if (!player.isAlive()) continue;

            UUID uuid = player.getUUID();
            if (rendered.contains(uuid)) continue;
            rendered.add(uuid);

            String rawName = player.getGameProfile().name();
            boolean isFriend = FriendManager.isFriend(rawName);

            double wx = player.getX();
            double wy = player.getY() + player.getEyeHeight() + 0.4;
            double wz = player.getZ();

            float[] screen = WorldToScreen.project(wx, wy, wz);
            if (screen == null) continue;

            int screenX = Math.round(screen[0]);
            int screenY = Math.round(screen[1]);

            // HP
            String hpStr = "";
            if (showHealth.isEnabled() && player instanceof LivingEntity le) {
                int hp = Math.round(le.getHealth());
                int abs = Math.round(le.getAbsorptionAmount());
                hpStr = abs > 0 ? " [" + hp + "," + abs + "]" : " [" + hp + "]";
            }

            // Пинг
            String pingStr = "";
            if (showPing.isEnabled()) {
                int ping = getPing(player);
                if (ping >= 0) pingStr = " " + ping + "ms";
            }

            // Тег F для друга
            String friendF = isFriend ? "F " : "";

            // Теги
            boolean friendFEnabled = isFriend && showHealth.isEnabled();

            // Ширины
            float size = 5.5f; // Размер текста
            int wF = friendFEnabled ? mc.font.width(friendF) : 0;
            int wName = mc.font.width(rawName);
            int wHp = hpStr.isEmpty() ? 0 : mc.font.width(hpStr);
            int wPing = pingStr.isEmpty() ? 0 : mc.font.width(pingStr);

            int totalW = wF + wName + wHp + wPing + (friendFEnabled ? SEP : 0) + (hpStr.isEmpty() ? 0 : SEP) + (pingStr.isEmpty() ? 0 : SEP);

            int headPx = showHead.isEnabled() ? mc.font.lineHeight : 0;
            int headGap = showHead.isEnabled() ? 2 : 0;

            int fontH = mc.font.lineHeight;
            int bgW = totalW + headPx + headGap + PAD_X * 2;
            int bgH = fontH + PAD_Y * 2;
            int bgX = screenX - bgW / 2;
            int bgY = screenY - bgH - 2;

            // Фон
            Color bg = bgColor.getColor();
            RenderUtil.rect(bgX, bgY, bgW, bgH, BG_RADIUS, new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), (int)(bg.getAlpha() * 0.7f)));

            // Рисуем скин головы
            //if (showHead.isEnabled()) {
                //drawSkinHead(player, bgX + 2, bgY + 2, fontH);
            //}

            int tx = bgX + 2 + headPx + (showHead.isEnabled() ? 2 : 0);

            // Тег F
            if (friendFEnabled) {
                Color fColor = friendTagColor.getColor();
                ctx.text(mc.font, friendF, tx, bgY + PAD_Y, rgba(fColor.getRed(), fColor.getGreen(), fColor.getBlue(), fColor.getAlpha()));
                tx += wF;
            }

            // Имя
            Color nColor = nameColor.getColor();
            ctx.text(mc.font, rawName, tx, bgY + PAD_Y, rgba(nColor.getRed(), nColor.getGreen(), nColor.getBlue(), nColor.getAlpha()));
            tx += wName;

            // HP
            if (!hpStr.isEmpty()) {
                tx += SEP;
                Color hColor = hpColor.getColor();
                ctx.text(mc.font, hpStr, tx, bgY + PAD_Y, rgba(hColor.getRed(), hColor.getGreen(), hColor.getBlue(), hColor.getAlpha()));
                tx += wHp;
            }

            // Пинг
            if (!pingStr.isEmpty()) {
                tx += SEP;
                int pColor = rgba(120, 120, 140, 255);
                ctx.text(mc.font, pingStr, tx, bgY + PAD_Y, pColor);
            }

            // Броня
            if (showArmor.isEnabled()) {
                drawArmorRow(ctx, player, bgX + headPx + headGap + wF + wName + wHp + wPing + 4, bgY - 14, size);
            }
        }
    }

    //private void drawSkinHead(Player player, float x, float y, float size) {
        //try {
           // Minecraft mc = Minecraft.getInstance();
            // info = mc.getConnection().getPlayerInfo(player.getUUID());
            // (info == null) return;

            // In 26.2 Mojang mappings, skin textures are available directly on PlayerInfo
           // Identifier skinTexture = info.getSkin().texture();
            //if (skinTexture == null) return;

            //float u0 = 8f  / 64f;
           // float v0 = 8f  / 64f;
            //float u1 = 16f / 64f;
            //float v1 = 16f / 64f;

            // TODO: texture rendering via RenderUtil or direct draw
            // For now, skip head rendering
        //} catch (Exception ignored) {}
   // }

    private void drawArmorRow(GuiGraphicsExtractor ctx, Player player, float centerX, float startY, float size) {
        List<ItemStack> items = new ArrayList<>();

        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) items.add(mainHand);

        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack s = player.getItemBySlot(slot);
            if (!s.isEmpty()) items.add(s);
        }

        if (items.isEmpty()) return;

        float itemScale = 0.5f;
        float itemSize  = 16f * itemScale;
        float gap       = 1.5f;
        float totalWidth = items.size() * itemSize + (items.size() - 1) * gap;
        float rowHeight  = itemSize + 2f;
        float drawY = startY - rowHeight;
        float drawX = centerX - totalWidth / 2f;

        // TODO: blur and texture rendering
        // For now, skip armor rendering
    }

    private static int rgba(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int getPing(Player player) {
        if (mc.getConnection() == null) return -1;
        PlayerInfo info = mc.getConnection().getPlayerInfo(player.getUUID());
        return info != null ? info.getLatency() : -1;
    }
}
