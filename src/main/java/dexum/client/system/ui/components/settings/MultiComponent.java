package dexum.client.system.ui.components.settings;

import dexum.client.system.settings.impl.MultiSetting;
import dexum.client.system.ui.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class MultiComponent extends SettingComponent {
    private final MultiSetting multiSetting;
    private boolean expanded = false;

    public MultiComponent(MultiSetting setting, int width) {
        super(setting, width);
        this.multiSetting = setting;
    }

    @Override
    public void render(GuiGraphicsExtractor context, int x, int y, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();

        boolean headerHovered = isHovered(mouseX, mouseY, x, y, width - 8, HEIGHT);
        if (headerHovered) RenderHelper.fillRoundRect(context, x, y, width - 8, HEIGHT, 4, 0x30FFFFFF);

        String arrowPrefix = expanded ? "v " : "> ";
        String fullHeader = arrowPrefix + setting.getName();
        int maxW = width - 12;
        String headerText = mc.font.width(fullHeader) > maxW
                ? mc.font.plainSubstrByWidth(fullHeader, maxW - mc.font.width("…")) + "…"
                : fullHeader;
        context.text(mc.font, headerText, x + 4, y + 3, 0xFFAAAAAA);

        if (expanded) {
            int optionY = y + HEIGHT;
            for (String option : multiSetting.getAllOptions()) {
                boolean optHov = isHovered(mouseX, mouseY, x + 8, optionY, width - 16, HEIGHT);
                if (optHov) RenderHelper.fillRoundRect(context, x + 8, optionY, width - 16, HEIGHT, 4, 0x30FFFFFF);

                int cbX = x + 12, cbY = optionY + 3, cbS = 8;
                RenderHelper.fillRoundRect(context, cbX, cbY, cbS, cbS, 2, 0xFF1a1a1a);
                if (multiSetting.isEnabled(option)) {
                    RenderHelper.fillRoundRect(context, cbX + 2, cbY + 2, cbS - 4, cbS - 4, 1, 0xFF4a9eff);
                }

                int textX = cbX + cbS + 4;
                int maxOW = x + width - 8 - textX;
                String optText = mc.font.width(option) > maxOW
                        ? mc.font.plainSubstrByWidth(option, maxOW - mc.font.width("…")) + "…"
                        : option;
                context.text(mc.font, optText, textX, optionY + 3, 0xFFAAAAAA);
                optionY += HEIGHT;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y) {
        if (button == 0 && isHovered((int) mouseX, (int) mouseY, x, y, width - 8, HEIGHT)) {
            expanded = !expanded;
            return true;
        }
        if (expanded && button == 0) {
            int optionY = y + HEIGHT;
            for (String option : multiSetting.getAllOptions()) {
                if (isHovered((int) mouseX, (int) mouseY, x + 8, optionY, width - 16, HEIGHT)) {
                    multiSetting.toggle(option);
                    return true;
                }
                optionY += HEIGHT;
            }
        }
        return false;
    }

    @Override
    public int getHeight() {
        return HEIGHT + (expanded ? multiSetting.getAllOptions().size() * HEIGHT : 0);
    }
}
