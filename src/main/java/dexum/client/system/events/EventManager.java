package dexum.client.system.events;

import dexum.client.system.api.Function;
import dexum.client.system.api.FunctionManager;
import dexum.client.system.events.impl.KeyEvent;
import dexum.client.system.ui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class EventManager {
    Minecraft mc = Minecraft.getInstance();

    @EventHandler
    public void onKey(KeyEvent event) {
        int key = event.getKey();
        int action = event.getAction();

        if (action == GLFW.GLFW_PRESS && mc.gui.screen() == null) {
            for (Function function : FunctionManager.getFunctions()) {
                if (function.getKeybind() == key) {
                    function.toggle();
                }
            }
        }
    }
}
