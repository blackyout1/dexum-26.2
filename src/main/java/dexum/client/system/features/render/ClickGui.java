package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.ui.ClickGuiScreen;
import org.lwjgl.glfw.GLFW;

@NewFunction(name = "ClickGui", desc = "Открывает графический интерфейс для управления функциями", category = Category.RENDER)
public class ClickGui extends Function {

    public ClickGui() {
        this.setKeybind(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onEnable() {
        // Не открывать GUI, если нет игрока/мира (например, при загрузке конфига до старта игры).
        if (nullCheck.all()) {
            setEnabled(false);
            return;
        }
        if (mc.gui.screen() == null) {
            mc.setScreenAndShow(new ClickGuiScreen());
        }
        // One-shot: модуль отключается сразу — новый setEnabled корректно обработает это.
        setEnabled(false);
    }
}