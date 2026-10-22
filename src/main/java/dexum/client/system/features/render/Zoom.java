package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.UpdateEvent;
import dexum.client.system.settings.impl.NumberSetting;
import org.lwjgl.glfw.GLFW;

@NewFunction(name = "Zoom", desc = "Приближение камеры — зажми биндклавишу", category = Category.RENDER)
public class Zoom extends Function {

    private final NumberSetting zoomFactor = new NumberSetting("Множитель", 4.0, 1.5, 10.0, 0.5);
    private final NumberSetting smooth     = new NumberSetting("Плавность", 8.0, 1.0, 20.0, 1.0);

    private double baseFov    = -1;
    private double currentFov = -1;
    private boolean wasZooming = false;

    public Zoom() {
        addSettings(zoomFactor, smooth);
        this.setKeybind(GLFW.GLFW_KEY_C);
    }

    @Override
    public void onEnable() {
        baseFov    = mc.options.fov().get();
        currentFov = baseFov;
    }

    @Override
    public void onDisable() {
        if (baseFov > 0) {
            mc.options.fov().set((int) Math.round(baseFov));
        }
        baseFov    = -1;
        currentFov = -1;
        wasZooming = false;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (nullCheck.player()) return;

        int keybind = this.getKeybind();
        boolean pressing = keybind != 0 && keybind != GLFW.GLFW_KEY_UNKNOWN
                && GLFW.glfwGetKey(mc.getWindow().handle(), keybind) == GLFW.GLFW_PRESS;

        if (baseFov < 0) {
            baseFov    = mc.options.fov().get();
            currentFov = baseFov;
        }

        double targetFov = pressing ? baseFov / zoomFactor.getValue() : baseFov;

        double spd = smooth.getValue() * 0.05;
        currentFov += (targetFov - currentFov) * Math.min(1.0, spd);

        mc.options.fov().set((int) Math.round(currentFov));

        if (!pressing) {
            int actualFov = mc.options.fov().get();
            if (Math.abs(actualFov - currentFov) < 1.0) {
                baseFov = actualFov;
            }
        }

        wasZooming = pressing;
    }
}
