package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventBus;
import dexum.client.system.ui.ClickGuiScreen;
import org.lwjgl.glfw.GLFW;

@NewFunction(name = "ClickGui", desc = "Открывает графический интерфейс для управления функциями", category = Category.RENDER)
public class ClickGui extends Function {

    public ClickGui() {
        this.setKeybind(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onEnable() {
        if (mc.gui.screen() == null) {
            mc.setScreenAndShow(new ClickGuiScreen());
        }
        // Immediately disable so the keybind can re-trigger on next press
        this.setEnabled(false);
        EventBus.unregister(this);
    }
}
