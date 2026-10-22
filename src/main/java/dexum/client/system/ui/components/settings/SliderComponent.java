package dexum.client.system.ui.components.settings;

import dexum.client.system.settings.impl.NumberSetting;
import dexum.client.system.ui.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class SliderComponent extends SettingComponent {
    private final NumberSetting numberSetting;
    private boolean dragging = false;

    private static final int SLIDER_H = 4;
    private static final int KNOB_W   = 4;

    public SliderComponent(NumberSetting setting, int width) {
        super(setting, width);
        this.numberSetting = setting;
    }

    @Override
    public void render(GuiGraphicsExtractor context, int x, int y, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();

        String fullText = setting.getName() + ": " + formatValue(numberSetting.getValue());
        int maxW = width - 8;
        String text = mc.font.width(fullText) > maxW
                ? mc.font.plainSubstrByWidth(fullText, maxW - mc.font.width("…")) + "…"
                : fullText;
        context.text(mc.font, text, x + 4, y + 2, 0xFFAAAAAA);

        int sliderX = x + 4;
        int sliderY = y + HEIGHT - SLIDER_H - 2;
        int sliderW = width - 12;

        RenderHelper.fillRoundRect(context, sliderX, sliderY, sliderW, SLIDER_H, SLIDER_H / 2, 0xFF3a3a3a);

        double pct = (numberSetting.getValue() - numberSetting.getMin())
                   / (numberSetting.getMax() - numberSetting.getMin());
        int prog = (int)(sliderW * pct);
        if (prog > 0) {
            RenderHelper.fillRoundRect(context, sliderX, sliderY, prog, SLIDER_H, SLIDER_H / 2, 0xFF4a9eff);
        }

        int knobX = sliderX + prog - KNOB_W / 2;
        RenderHelper.fillRoundRect(context, knobX, sliderY - 2, KNOB_W, SLIDER_H + 4, 2, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y) {
        if (button != 0) return false;
        int sliderX = x + 4, sliderY = y + HEIGHT - SLIDER_H - 2, sliderW = width - 12;
        if (mouseX >= sliderX && mouseX <= sliderX + sliderW
                && mouseY >= sliderY - 4 && mouseY <= sliderY + SLIDER_H + 4) {
            dragging = true;
            updateValue(mouseX, x);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button, int x, int y) {
        if (button == 0 && dragging) { dragging = false; return true; }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, int x, int y) {
        if (dragging) { updateValue(mouseX, x); return true; }
        return false;
    }

    private void updateValue(double mouseX, int x) {
        int sliderX = x + 4, sliderW = width - 12;
        double pct = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / sliderW));
        double raw = numberSetting.getMin() + (numberSetting.getMax() - numberSetting.getMin()) * pct;
        double step = numberSetting.getStep();
        numberSetting.setValue(Math.round(raw / step) * step);
    }

    private String formatValue(double value) {
        if (value == (long) value) return String.format("%d", (long) value);
        return String.format("%.2f", value);
    }

    @Override
    public int getHeight() { return HEIGHT + 4; }
}
