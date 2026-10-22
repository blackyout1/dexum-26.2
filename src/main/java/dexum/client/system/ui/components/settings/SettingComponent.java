package dexum.client.system.ui.components.settings;

import dexum.client.system.settings.Setting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class SettingComponent {
    protected final Setting setting;
    protected final int width;
    protected static final int HEIGHT = 14;

    public SettingComponent(Setting setting, int width) {
        this.setting = setting;
        this.width = width;
    }

    public abstract void render(GuiGraphicsExtractor context, int x, int y, int mouseX, int mouseY, float delta);
    public abstract boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y);

    public boolean mouseReleased(double mouseX, double mouseY, int button, int x, int y) { return false; }
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, int x, int y) { return false; }
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    public int getHeight() { return HEIGHT; }
    public boolean isVisible() { return setting.isVisible(); }

    protected boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
