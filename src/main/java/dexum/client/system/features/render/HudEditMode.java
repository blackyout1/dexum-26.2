package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.hud.HudElementManager;

/**
 * Feature to enable/disable HUD element editing mode.
 * When enabled, all HUD elements become draggable and their positions can be adjusted.
 */
@NewFunction(name = "HUD Edit Mode", desc = "Enable dragging HUD elements", category = Category.RENDER)
public class HudEditMode extends Function {

    @Override
    public void onEnable() {
        HudElementManager.initialize();
        HudElementManager.setEditModeActive(true);
    }

    @Override
    public void onDisable() {
        HudElementManager.setEditModeActive(false);
    }
}
