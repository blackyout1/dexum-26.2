package dexum.client.system.ui.components.settings;

import dexum.client.system.settings.impl.ModeSetting;
import dexum.client.system.ui.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ModeComponent extends SettingComponent {
    private final ModeSetting modeSetting;

    public ModeComponent(ModeSetting setting, int width) {
        super(setting, width);
        this.modeSetting = setting;
    }

    @Override
    public void render(GuiGraphicsExtractor context, int x, int y, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();
        boolean hovered = isHovered(mouseX, mouseY, x, y, width - 8, HEIGHT);
        if (hovered) RenderHelper.fillRoundRect(context, x, y, width - 8, HEIGHT, 4, 0x30FFFFFF);

        String fullText = setting.getName() + ": " + modeSetting.getActiveMode();
        int maxTextWidth = width - 8;
        String text = mc.font.width(fullText) > maxTextWidth
                ? mc.font.plainSubstrByWidth(fullText, maxTextWidth - mc.font.width("…")) + "…"
                : fullText;
        context.text(mc.font, text, x + 4, y + 3, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y) {
        if (isHovered((int) mouseX, (int) mouseY, x, y, width - 8, HEIGHT)) {
            if (button == 0) { cycleMode(true); return true; }
            if (button == 1) { cycleMode(false); return true; }
        }
        return false;
    }

    private void cycleMode(boolean forward) {
        var modes = modeSetting.getModes();
        int i = modes.indexOf(modeSetting.getActiveMode());
        modeSetting.setMode(modes.get(forward ? (i + 1) % modes.size() : (i - 1 + modes.size()) % modes.size()));
    }
}
