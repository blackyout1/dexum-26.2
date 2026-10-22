package dexum.client.system.ui.components.settings;

import dexum.client.system.settings.impl.BooleanSetting;
import dexum.client.system.ui.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class BooleanComponent extends SettingComponent {
    private final BooleanSetting booleanSetting;

    private static final int   TW = 22;
    private static final int   TH = 11;
    private static final int   KD = 7;
    private static final int   KP = 2;
    private static final float ANIM_SPEED = 12f;
    private float animation = 0f;

    private static final int OFF_COLOR  = 0xFF3a3a3a;
    private static final int ON_COLOR   = 0xFF4a9eff;
    private static final int KNOB_COLOR = 0xFFFFFFFF;

    public BooleanComponent(BooleanSetting setting, int width) {
        super(setting, width);
        this.booleanSetting = setting;
        this.animation = setting.isEnabled() ? 1f : 0f;
    }

    @Override
    public void render(GuiGraphicsExtractor context, int x, int y, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();

        float target = booleanSetting.isEnabled() ? 1f : 0f;
        float diff = target - animation;
        animation = Math.abs(diff) < 0.005f ? target : animation + diff * Math.min(1f, ANIM_SPEED * delta);

        context.text(mc.font, setting.getName(), x + 4, y + 3, 0xFFAAAAAA);

        int tx = x + width - TW - 6;
        int ty = y + (HEIGHT - TH) / 2;

        int bg = lerpColor(OFF_COLOR, ON_COLOR, animation);
        RenderHelper.fillRoundRect(context, tx, ty, TW, TH, TH / 2, bg);

        float travel = TW - KP * 2 - KD;
        int kx = tx + KP + Math.round(travel * animation);
        int ky = ty + (TH - KD) / 2;
        RenderHelper.fillRoundRect(context, kx, ky, KD, KD, KD / 2, KNOB_COLOR);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y) {
        if (button == 0 && isHovered((int) mouseX, (int) mouseY, x, y, width - 4, HEIGHT)) {
            booleanSetting.setEnabled(!booleanSetting.isEnabled());
            return true;
        }
        return false;
    }

    private static int lerpColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (a >> 24) & 0xFF, ra = (a >> 16) & 0xFF, ga = (a >> 8) & 0xFF, ba = a & 0xFF;
        int ab = (b >> 24) & 0xFF, rb = (b >> 16) & 0xFF, gb = (b >> 8) & 0xFF, bb = b & 0xFF;
        return ((aa + (int)((ab - aa) * t)) << 24) | ((ra + (int)((rb - ra) * t)) << 16)
             | ((ga + (int)((gb - ga) * t)) << 8) | (ba + (int)((bb - ba) * t));
    }
}
